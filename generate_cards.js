const sharp = require('sharp');
const fs = require('fs');
const path = require('path');

const WIDTH = 1122; // 3.5" + 2 * 36px bleed
const HEIGHT = 822; // 2.5" + 2 * 36px bleed
const BLEED = 36;
const CARD_WIDTH = 1050; // 3.5" at 300 DPI
const CARD_HEIGHT = 750; // 2.5" at 300 DPI

const CORNER_RADIUS = 38; // ~1/8 inch at 300 DPI
const INSET = 60; // Padding from card edge to symbols
const STROKE_WIDTH = 20;
const STRIPE_WIDTH = 10;

const COLORS = [
  '#2196F3', // Blue
  '#FFB300', // Orange
  '#F44336'  // Red
];

const OUTPUT_DIR = path.join(__dirname, 'output', 'cards');

if (!fs.existsSync(OUTPUT_DIR)) {
  fs.mkdirSync(OUTPUT_DIR, { recursive: true });
}

function getShapePath(shapeId, x, y, size) {
  const cx = x + size / 2;
  const cy = y + size / 2;
  const r = size / 2;

  switch (shapeId) {
    case 0: // Square
      return `<rect x="${x}" y="${y}" width="${size}" height="${size}" />`;
    case 1: // Circle
      return `<circle cx="${cx}" cy="${cy}" r="${r}" />`;
    case 2: // Triangle
      return `<path d="M ${x},${y + size} L ${cx},${y} L ${x + size},${y + size} Z" />`;
    default:
      return '';
  }
}

function generateSVG(number, shape, pattern, colorId) {
  const color = COLORS[colorId];

  const halfSideLength = CARD_WIDTH / 10;
  const size = halfSideLength * 2;
  const gap = halfSideLength / 2;

  const symbolPositions = [];
  const centerX = WIDTH / 2;
  const centerY = HEIGHT / 2;

  switch (number) {
    case 0:
      symbolPositions.push({ x: centerX - halfSideLength, y: centerY - halfSideLength });
      break;
    case 1:
      symbolPositions.push({ x: centerX - gap / 2 - size, y: centerY - halfSideLength });
      symbolPositions.push({ x: centerX + gap / 2, y: centerY - halfSideLength });
      break;
    case 2:
      symbolPositions.push({ x: centerX - gap - size * 1.5, y: centerY - halfSideLength });
      symbolPositions.push({ x: centerX - halfSideLength, y: centerY - halfSideLength });
      symbolPositions.push({ x: centerX + gap + size * 0.5, y: centerY - halfSideLength });
      break;
  }

  let defs = '';
  let fillAttr = 'none';

  if (pattern === 1) { // Shaded
    const patternId = `pattern-${colorId}`;
    defs = `
      <defs>
        <pattern id="${patternId}" width="${STRIPE_WIDTH * 2}" height="1" patternUnits="userSpaceOnUse">
          <rect x="${STRIPE_WIDTH}" y="0" width="${STRIPE_WIDTH}" height="1" fill="${color}" />
        </pattern>
      </defs>`;
    fillAttr = `url(#${patternId})`;
  } else if (pattern === 2) { // Solid
    fillAttr = color;
  }

  let symbols = '';
  symbolPositions.forEach(pos => {
    symbols += getShapePath(shape, pos.x, pos.y, size);
  });

  return `
    <svg width="${WIDTH}" height="${HEIGHT}" viewBox="0 0 ${WIDTH} ${HEIGHT}" xmlns="http://www.w3.org/2000/svg">
      ${defs}
      <!-- Bleed area and background -->
      <rect x="0" y="0" width="${WIDTH}" height="${HEIGHT}" fill="white" />
      <!-- Card border (at cut line) -->
      <rect x="${BLEED}" y="${BLEED}" width="${CARD_WIDTH}" height="${CARD_HEIGHT}" rx="${CORNER_RADIUS}" fill="none" stroke="#C4C7CC" stroke-width="1" />
      <g fill="${fillAttr}" stroke="${color}" stroke-width="${STROKE_WIDTH}">
        ${symbols}
      </g>
    </svg>
  `;
}

async function run() {
  console.log('Generating 81 poker-sized cards with bleed...');
  for (let n = 0; n < 3; n++) {
    for (let s = 0; s < 3; s++) {
      for (let p = 0; p < 3; p++) {
        for (let c = 0; c < 3; c++) {
          const svg = generateSVG(n, s, p, c);
          const baseName = `card_${n}_${s}_${p}_${c}`;
          const svgPath = path.join(OUTPUT_DIR, `${baseName}.svg`);
          const pngPath = path.join(OUTPUT_DIR, `${baseName}.png`);

          fs.writeFileSync(svgPath, svg);
          await sharp(Buffer.from(svg))
            .toFile(pngPath);
        }
      }
    }
  }
  console.log('Done!');
}

run().catch(err => {
  console.error(err);
  process.exit(1);
});
