const sharp = require('sharp');
const fs = require('fs');
const path = require('path');

const WIDTH = 1050;
const HEIGHT = 649;
const CORNER_RADIUS = 26; // 4dp * 6.5 approx
const INSET = 52; // 8dp * 6.5 approx
const STROKE_WIDTH = 20; // 3dp * 6.5 approx
const STRIPE_WIDTH = 10; // 1.5dp * 6.5 approx

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
  const cardWidth = WIDTH - 2 * INSET;
  const cardHeight = HEIGHT - 2 * INSET;

  const halfSideLength = WIDTH / 10;
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
      <rect x="${INSET}" y="${INSET}" width="${cardWidth}" height="${cardHeight}" rx="${CORNER_RADIUS}" fill="white" stroke="#C4C7CC" stroke-width="1" />
      <g fill="${fillAttr}" stroke="${color}" stroke-width="${STROKE_WIDTH}">
        ${symbols}
      </g>
    </svg>
  `;
}

async function run() {
  console.log('Generating 81 cards...');
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
