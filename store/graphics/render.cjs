// Reproduce PNGs with Node.js and sharp. The SVGs extend the existing app icon.
const sharp = require('sharp');
const path = require('node:path');
async function main() {
  for (const name of ['icon', 'feature']) {
    await sharp(path.join(__dirname, name + '.svg')).png().toFile(path.join(__dirname, name + '.png'));
  }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
