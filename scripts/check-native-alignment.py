"""Check 64-bit ELF LOAD alignment in an APK/AAB (not ZIP packaging alignment)."""
import struct
import sys
import zipfile


def check(path):
    failures = []
    checked = 0
    with zipfile.ZipFile(path) as archive:
        for name in archive.namelist():
            if not name.endswith('.so'):
                continue
            data = archive.read(name)
            if data[:4] != b'\x7fELF':
                failures.append(f'{name}: not an ELF library')
                continue
            if data[4] != 2:
                continue  # The Play 16 KB requirement concerns 64-bit devices.
            endian = '<' if data[5] == 1 else '>'
            offset = struct.unpack_from(endian + 'Q', data, 32)[0]
            size, count = struct.unpack_from(endian + 'HH', data, 54)
            loads = []
            for index in range(count):
                header = offset + index * size
                if struct.unpack_from(endian + 'I', data, header)[0] == 1:
                    file_offset, virtual = struct.unpack_from(endian + 'QQ', data, header + 8)
                    alignment = struct.unpack_from(endian + 'Q', data, header + 48)[0]
                    loads.append(alignment)
                    if alignment < 16384 or alignment & (alignment - 1) or (virtual - file_offset) % 16384:
                        failures.append(f'{name}: LOAD segment is not 16 KB aligned')
            if not loads:
                failures.append(f'{name}: no LOAD segments')
            checked += 1
            print(f'{name}: LOAD alignments {loads}')
    if failures:
        raise SystemExit('\n'.join(failures))
    print(f'PASS: {checked} 64-bit libraries; verify generated APK ZIP alignment and runtime separately.')


if __name__ == '__main__':
    if len(sys.argv) != 2:
        raise SystemExit('Usage: python check-native-alignment.py app.aab')
    check(sys.argv[1])
