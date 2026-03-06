const sharp = require('sharp');
const fs = require('fs');
const path = require('path');

const svgFile = 'app_icon.svg';
const outputDir = 'fastlane/metadata/android/en-US/images';
const achievementsDir = path.join(outputDir, 'achievements');

if (!fs.existsSync(achievementsDir)) {
  fs.mkdirSync(achievementsDir, { recursive: true });
}

async function generateFeatureGraphic() {
  const width = 1024;
  const height = 500;

  // Create a 1024x500 background
  const background = await sharp({
    create: {
      width,
      height,
      channels: 4,
      background: { r: 251, g: 252, b: 254, alpha: 1 } // colorSurface
    }
  }).png().toBuffer();

  // Load the app icon and resize it
  const icon = await sharp(svgFile)
    .resize(400, 400)
    .toBuffer();

  // Create SVG text for "Triples"
  const textSvg = Buffer.from(`
    <svg width="500" height="200" viewBox="0 0 500 200" xmlns="http://www.w3.org/2000/svg">
      <text x="0" y="150" font-family="sans-serif" font-size="120" font-weight="bold" fill="#00677C">Triples</text>
    </svg>
  `);

  await sharp(background)
    .composite([
      { input: icon, left: 50, top: 50 },
      { input: textSvg, left: 500, top: 150 }
    ])
    .toFile(path.join(outputDir, 'feature_graphic.png'));

  console.log('Generated feature_graphic.png');
}

async function generateAchievements() {
  const csv = fs.readFileSync('AchievementsMetadata.csv', 'utf8');
  const lines = csv.split('\n').filter(line => line.trim() !== '');

  for (const line of lines) {
    const parts = line.split(',');
    if (parts.length < 2) continue;
    const name = parts[0];
    const fileName = name.replace(/[: <]/g, '_').replace(/__/g, '_').toLowerCase() + '.png';

    let template = '';
    if (name.startsWith('Classic: ') && name.includes('game')) {
       template = createClassicTemplate(name.split(' ')[1]);
    } else if (name.startsWith('Arcade: ') && name.includes('game')) {
       template = createArcadeTemplate(name.split(' ')[1]);
    } else if (name.startsWith('Classic: <')) {
       template = createSpeedTemplate(name.split(' ')[2]);
    } else if (name.startsWith('Arcade: ') && name.includes('triples')) {
       template = createTriplesTemplate(name.split(' ')[1]);
    }

    if (template) {
      await sharp(Buffer.from(template))
        .resize(512, 512)
        .toFile(path.join(achievementsDir, fileName));
      console.log(`Generated achievement icon: ${fileName}`);
    }
  }
}

function createClassicTemplate(count) {
  return `
<svg width="512" height="512" viewBox="0 0 512 512" xmlns="http://www.w3.org/2000/svg">
  <rect width="512" height="512" rx="64" fill="#1976D2"/>
  <g transform="translate(136, 184)">
    <rect x="8" y="8" width="240" height="144" rx="8" fill="#B0B0B0" opacity="0.5"/>
    <rect x="0" y="0" width="240" height="144" rx="8" fill="#FFFFFF" stroke="#C4C7CC" stroke-width="2"/>
    <rect x="26" y="44" width="56" height="56" fill="#2196F3"/>
    <rect x="92" y="44" width="56" height="56" fill="#2196F3"/>
    <rect x="158" y="44" width="56" height="56" fill="#2196F3"/>
  </g>
  <text x="256" y="450" font-family="sans-serif" font-size="60" font-weight="bold" fill="#FFFFFF" text-anchor="middle">${count}</text>
</svg>`;
}

function createArcadeTemplate(count) {
  return `
<svg width="512" height="512" viewBox="0 0 512 512" xmlns="http://www.w3.org/2000/svg">
  <rect width="512" height="512" rx="64" fill="#388E3C"/>
  <g transform="translate(136, 184)">
    <rect x="8" y="8" width="240" height="144" rx="8" fill="#B0B0B0" opacity="0.5"/>
    <rect x="0" y="0" width="240" height="144" rx="8" fill="#FFFFFF" stroke="#C4C7CC" stroke-width="2"/>
    <path d="M 59 100 L 87 44 L 115 100 Z" fill="#FF9800"/>
    <path d="M 125 100 L 153 44 L 181 100 Z" fill="#FF9800"/>
  </g>
  <text x="256" y="450" font-family="sans-serif" font-size="60" font-weight="bold" fill="#FFFFFF" text-anchor="middle">${count}</text>
</svg>`;
}

function createSpeedTemplate(time) {
  return `
<svg width="512" height="512" viewBox="0 0 512 512" xmlns="http://www.w3.org/2000/svg">
  <rect width="512" height="512" rx="64" fill="#7B1FA2"/>
  <g transform="translate(136, 184)">
    <rect x="8" y="8" width="240" height="144" rx="8" fill="#B0B0B0" opacity="0.5"/>
    <rect x="0" y="0" width="240" height="144" rx="8" fill="#FFFFFF" stroke="#C4C7CC" stroke-width="2"/>
    <circle cx="120" cy="72" r="28" fill="#F44336"/>
  </g>
  <text x="256" y="450" font-family="sans-serif" font-size="60" font-weight="bold" fill="#FFFFFF" text-anchor="middle">&lt; ${time}</text>
</svg>`;
}

function createTriplesTemplate(count) {
  return `
<svg width="512" height="512" viewBox="0 0 512 512" xmlns="http://www.w3.org/2000/svg">
  <rect width="512" height="512" rx="64" fill="#FF9800"/>
  <g transform="translate(136, 184)">
    <rect x="8" y="8" width="240" height="144" rx="8" fill="#B0B0B0" opacity="0.5"/>
    <rect x="0" y="0" width="240" height="144" rx="8" fill="#FFFFFF" stroke="#C4C7CC" stroke-width="2"/>
    <path d="M 59 100 L 87 44 L 115 100 Z" fill="#388E3C"/>
    <path d="M 125 100 L 153 44 L 181 100 Z" fill="#388E3C"/>
  </g>
  <text x="256" y="450" font-family="sans-serif" font-size="60" font-weight="bold" fill="#FFFFFF" text-anchor="middle">${count} Triples</text>
</svg>`;
}

async function run() {
  await generateFeatureGraphic();
  await generateAchievements();
}

run().catch(console.error);
