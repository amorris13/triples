const sharp = require('sharp');
const fs = require('fs');
const path = require('path');

const svgFile = 'app_icon.svg';
const baseResDir = 'app/src/main/res';

const densities = {
  'mdpi': 48,
  'hdpi': 72,
  'xhdpi': 96,
  'xxhdpi': 144,
  'xxxhdpi': 192
};

async function generateIcons() {
  for (const [density, size] of Object.entries(densities)) {
    const dir = path.join(baseResDir, `drawable-${density}`);
    if (!fs.existsSync(dir)) {
      fs.mkdirSync(dir, { recursive: true });
    }

    // ic_launcher.png (with white background) -> for Home Screen
    await sharp(svgFile)
      .resize(size, size)
      .flatten({ background: { r: 255, g: 255, b: 255 } })
      .toFile(path.join(dir, 'ic_launcher.png'));

    // launcher.png (transparent) -> for in-app use (ActionBar etc)
    await sharp(svgFile)
      .resize(size, size)
      .toFile(path.join(dir, 'launcher.png'));

    console.log(`Generated icons for ${density} (${size}x${size})`);
  }
}

generateIcons().catch(console.error);
