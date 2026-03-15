const sharp = require('sharp');
const fs = require('fs');
const path = require('path');

const WIDTH = 1122; // 3.5" + 2 * 36px bleed
const HEIGHT = 822; // 2.5" + 2 * 36px bleed
const BLEED = 36;
const CARD_WIDTH = 1050; // 3.5" at 300 DPI
const CARD_HEIGHT = 750; // 2.5" at 300 DPI

const CORNER_RADIUS = 38; // ~1/8 inch at 300 DPI
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

function getShapePath(shapeId, x, y, size, fill = null, stroke = null, strokeWidth = null) {
  const cx = x + size / 2;
  const cy = y + size / 2;
  const r = size / 2;
  const fillAttr = fill ? `fill="${fill}"` : '';
  const strokeAttr = stroke ? `stroke="${stroke}"` : '';
  const strokeWidthAttr = strokeWidth ? `stroke-width="${strokeWidth}"` : '';
  const attrs = `${fillAttr} ${strokeAttr} ${strokeWidthAttr}`;

  switch (shapeId) {
    case 0: // Square
      return `<rect x="${x}" y="${y}" width="${size}" height="${size}" ${attrs} />`;
    case 1: // Circle
      return `<circle cx="${cx}" cy="${cy}" r="${r}" ${attrs} />`;
    case 2: // Triangle
      return `<path d="M ${x},${y + size} L ${cx},${y} L ${x + size},${y + size} Z" ${attrs} />`;
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
      <rect x="0" y="0" width="${WIDTH}" height="${HEIGHT}" fill="white" />
      <rect x="${BLEED}" y="${BLEED}" width="${CARD_WIDTH}" height="${CARD_HEIGHT}" rx="${CORNER_RADIUS}" fill="none" stroke="#C4C7CC" stroke-width="1" />
      <g fill="${fillAttr}" stroke="${color}" stroke-width="${STROKE_WIDTH}">
        ${symbols}
      </g>
    </svg>
  `;
}

function generateCardBack() {
  const bgColor = '#F8F6F0';
  const tealColor = '#00424E';
  const symbolSize = 25;
  const spacing = 45;

  let symbols = '';
  const voidHalfWidth = 320;
  const voidHalfHeight = 110;
  const voidCornerRadius = 50;
  const fadeWidth = 150;

  for (let x = -spacing; x < WIDTH + spacing; x += spacing) {
    for (let y = -spacing; y < HEIGHT + spacing; y += spacing) {
      const offsetX = (Math.floor(y / spacing) % 2 === 0) ? 0 : spacing / 2;
      const posX = x + offsetX;

      const dx = Math.abs(posX - WIDTH / 2);
      const dy = Math.abs(y - HEIGHT / 2);

      // Calculate distance from a rounded rectangle
      const qx = Math.max(dx - voidHalfWidth + voidCornerRadius, 0);
      const qy = Math.max(dy - voidHalfHeight + voidCornerRadius, 0);
      const dist = Math.sqrt(qx * qx + qy * qy) - voidCornerRadius;

      let opacity = 1;
      if (dist < 0) {
        opacity = 0;
      } else if (dist < fadeWidth) {
        opacity = dist / fadeWidth;
      }

      if (opacity > 0) {
        const shapeId = Math.floor(Math.random() * 3);
        let color;
        // Match reference image: Square -> Blue, Circle -> Red, Triangle -> Orange
        if (shapeId === 0) color = COLORS[0]; // Square -> Blue
        else if (shapeId === 1) color = COLORS[2]; // Circle -> Red
        else color = COLORS[1]; // Triangle -> Orange

        const rotation = (Math.random() - 0.5) * 40; // Reduced rotation
        symbols += `<g transform="translate(${posX},${y}) rotate(${rotation})">
          ${getShapePath(shapeId, -symbolSize/2, -symbolSize/2, symbolSize, color, null, null)}
        </g>`;
      }
    }
  }

  return `
    <svg width="${WIDTH}" height="${HEIGHT}" viewBox="0 0 ${WIDTH} ${HEIGHT}" xmlns="http://www.w3.org/2000/svg">
      <rect x="0" y="0" width="${WIDTH}" height="${HEIGHT}" fill="${bgColor}" />

      <!-- Symbol pattern -->
      <g opacity="0.4">
        ${symbols}
      </g>

      <!-- Inner frame -->
      <rect x="${BLEED + 20}" y="${BLEED + 20}" width="${CARD_WIDTH - 40}" height="${CARD_HEIGHT - 40}" rx="${CORNER_RADIUS - 10}" fill="none" stroke="${tealColor}" stroke-width="5" />
      <rect x="${BLEED + 32}" y="${BLEED + 32}" width="${CARD_WIDTH - 64}" height="${CARD_HEIGHT - 64}" rx="${CORNER_RADIUS - 16}" fill="none" stroke="${tealColor}" stroke-width="2" />

      <!-- Center Logo -->
      <text x="50%" y="50%" dy=".35em" text-anchor="middle" font-family="sans-serif" font-weight="bold" font-size="140" fill="${tealColor}">Triples</text>
    </svg>
  `;
}

async function run() {
  console.log('Generating 81 poker-sized cards and card back...');

  // Generate 81 cards
  for (let n = 0; n < 3; n++) {
    for (let s = 0; s < 3; s++) {
      for (let p = 0; p < 3; p++) {
        for (let c = 0; c < 3; c++) {
          const svg = generateSVG(n, s, p, c);
          const baseName = `card_${n}_${s}_${p}_${c}`;
          const svgPath = path.join(OUTPUT_DIR, `${baseName}.svg`);
          const pngPath = path.join(OUTPUT_DIR, `${baseName}.png`);

          fs.writeFileSync(svgPath, svg);
          await sharp(Buffer.from(svg)).toFile(pngPath);
        }
      }
    }
  }

  // Generate card back
  const backSvg = generateCardBack();
  const backSvgPath = path.join(OUTPUT_DIR, 'card_back.svg');
  const backPngPath = path.join(OUTPUT_DIR, 'card_back.png');
  fs.writeFileSync(backSvgPath, backSvg);
  await sharp(Buffer.from(backSvg)).toFile(backPngPath);

  console.log('Done!');
}

run().catch(err => {
  console.error(err);
  process.exit(1);
});
