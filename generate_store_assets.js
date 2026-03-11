const sharp = require('sharp');
const fs = require('fs');
const path = require('path');

const svgFile = 'app_icon.svg';
const outputDir = 'fastlane/metadata/android/en-US/images';
const achievementsDir = path.join(outputDir, 'achievements');

const placeholders = [
  "CgkI3f-DytEMEAIQBg",
  "CgkI3f-DytEMEAIQCw",
  "CgkI3f-DytEMEAIQDA",
  "CgkI3f-DytEMEAIQDQ",
  "CgkI3f-DytEMEAIQDg",
  "CgkI3f-DytEMEAIQDw"
];

if (!fs.existsSync(achievementsDir)) {
  fs.mkdirSync(achievementsDir, { recursive: true });
}

async function generateFeatureGraphic() {
  const width = 1024;
  const height = 500;

  const background = await sharp({
    create: {
      width,
      height,
      channels: 4,
      background: { r: 251, g: 252, b: 254, alpha: 1 } // colorSurface
    }
  }).png().toBuffer();

  const icon = await sharp(svgFile)
    .resize(400, 400)
    .toBuffer();

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
  const mappings = [];

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    const parts = line.split(',');
    if (parts.length < 2) continue;
    const name = parts[0];
    let fileName = '';
    let template = '';

    if (name.startsWith('Classic: ') && name.includes('game')) {
       const count = name.split(' ')[1];
       fileName = `classic_games_${count}.png`;
       template = createAchievementTemplate(count, 'classic', 'squares');
    } else if (name.startsWith('Arcade: ') && name.includes('game')) {
       const count = name.split(' ')[1];
       fileName = `arcade_games_${count}.png`;
       template = createAchievementTemplate(count, 'arcade', 'triangles');
    } else if (name.startsWith('Classic: <')) {
       const time = name.split(' ')[2];
       fileName = `classic_speed_${time}.png`;
       template = createAchievementTemplate(`&lt; ${time}`, 'classic', 'circle');
    } else if (name.startsWith('Arcade: ') && name.includes('triples')) {
       const count = name.split(' ')[1];
       fileName = `arcade_triples_${count}.png`;
       template = createAchievementTemplate(`${count} Triples`, 'arcade', 'triangles');
    } else if (name.startsWith('Daily: ') && name.includes('puzzle')) {
       const count = name.split(' ')[1];
       fileName = `daily_puzzles_${count}.png`;
       template = createAchievementTemplate(count, 'daily', 'hexagons');
    } else if (name.startsWith('Daily: ') && name.includes('streak')) {
       const streak = name.split(' ')[1];
       fileName = `daily_streak_${streak}.png`;
       template = createAchievementTemplate(`${streak} Days`, 'daily', 'hexagons');
    }

    if (template) {
      await sharp(Buffer.from(template))
        .resize(512, 512)
        .toFile(path.join(achievementsDir, fileName));
      console.log(`Generated achievement icon: ${fileName}`);

      let id = '';
      if (i < placeholders.length) {
        id = placeholders[i];
      }
      mappings.push(`${name},${fileName},${id}`);
    }
  }

  fs.writeFileSync(path.join(achievementsDir, 'AchievementsIconsMappings.csv'),
    'Achievement Name,Icon Filename,Play Games ID\n' + mappings.join('\n'));

  // Also copy Metadata
  fs.copyFileSync('AchievementsMetadata.csv', path.join(achievementsDir, 'AchievementsMetadata.csv'));
  console.log('Generated AchievementsIconsMappings.csv and copied metadata.');
}

function createAchievementTemplate(label, mode, symbolType) {
  let bgColor = '#1976D2';
  if (mode === 'arcade') {
    bgColor = '#388E3C';
  } else if (mode === 'daily') {
    bgColor = '#FF9800';
  }
  let symbol = '';

  if (symbolType === 'squares') {
    symbol = `
    <rect x="26" y="44" width="56" height="56" fill="#2196F3"/>
    <rect x="92" y="44" width="56" height="56" fill="#2196F3"/>
    <rect x="158" y="44" width="56" height="56" fill="#2196F3"/>`;
  } else if (symbolType === 'triangles') {
    symbol = `
    <path d="M 59 100 L 87 44 L 115 100 Z" fill="#FF9800"/>
    <path d="M 125 100 L 153 44 L 181 100 Z" fill="#FF9800"/>`;
  } else if (symbolType === 'triangles_alt') {
    symbol = `
    <path d="M 59 100 L 87 44 L 115 100 Z" fill="#FFFFFF" opacity="0.8"/>
    <path d="M 125 100 L 153 44 L 181 100 Z" fill="#FFFFFF" opacity="0.8"/>`;
  } else if (symbolType === 'circle') {
    symbol = `<circle cx="120" cy="72" r="27" fill="none" stroke="#F44336" stroke-width="8"/>`;
  } else if (symbolType === 'hexagons') {
    symbol = `
    <path d="M 26 58 L 54 44 L 82 58 L 82 86 L 54 100 L 26 86 Z" fill="#F44336"/>
    <path d="M 92 58 L 120 44 L 148 58 L 148 86 L 120 100 L 92 86 Z" fill="#F44336"/>
    <path d="M 158 58 L 186 44 L 214 58 L 214 86 L 186 100 L 158 86 Z" fill="#F44336"/>`;
  }

  return `
<svg width="512" height="512" viewBox="0 0 512 512" xmlns="http://www.w3.org/2000/svg">
  <rect width="512" height="512" rx="64" fill="${bgColor}"/>
  <g transform="translate(136, 184)">
    <rect x="8" y="8" width="240" height="144" rx="8" fill="#000000" opacity="0.2"/>
    <rect x="0" y="0" width="240" height="144" rx="8" fill="#FFFFFF" stroke="#C4C7CC" stroke-width="2"/>
    ${symbol}
  </g>
  <text x="256" y="450" font-family="sans-serif" font-size="60" font-weight="bold" fill="#FFFFFF" text-anchor="middle">${label}</text>
</svg>`;
}

async function run() {
  await generateFeatureGraphic();
  await generateAchievements();
}

run().catch(console.error);
