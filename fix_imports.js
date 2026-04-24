const fs = require('fs');
const path = require('path');

const featuresRoot = path.join('d:', 'founderlink', 'Frontend', 'src', 'app', 'features');
const appRoot = path.join('d:', 'founderlink', 'Frontend', 'src', 'app');

function getCorrectDepth(filePath, importedSegment) {
  const dir = path.dirname(filePath);
  const target = path.join(appRoot, importedSegment);
  const rel = path.relative(dir, target).replace(/\\/g, '/');
  return rel.startsWith('.') ? rel : './' + rel;
}

function walk(dir) {
  const items = fs.readdirSync(dir, { withFileTypes: true });
  for (const item of items) {
    const fp = path.join(dir, item.name);
    if (item.isDirectory()) {
      walk(fp);
    } else if (item.name.endsWith('.ts')) {
      let content = fs.readFileSync(fp, 'utf8');
      let changed = false;

      // Fix any number of ../ leading to core or shared
      content = content.replace(/(from\s+['"])([^'"]*\/)(core|shared)(\/[^'"]+)(['"])/g, (match, before, dots, seg, rest, after) => {
        const correct = getCorrectDepth(fp, seg + rest);
        const fixed = before + correct + after;
        if (fixed !== match) {
          changed = true;
          console.log(`[FIX] ${path.relative(appRoot, fp)}: ${dots}${seg}${rest} → ${correct}`);
        }
        return fixed;
      });
      
      // Fix environments paths similarly
      content = content.replace(/(from\s+['"])([^'"]*\/)(environments\/environment)(['"])/g, (match, before, dots, seg, after) => {
        const correct = getCorrectDepth(fp, '../../' + seg); // environments is 2 up from src/app
        // environments is at src/environments relative to app root would be '../environments'
        const dir2 = path.dirname(fp);
        const envPath = path.join('d:', 'founderlink', 'Frontend', 'src', seg);
        const rel = path.relative(dir2, envPath).replace(/\\/g, '/');
        const relFixed = rel.startsWith('.') ? rel : './' + rel;
        const fixed = before + relFixed + after;
        if (fixed !== match) {
          changed = true;
          console.log(`[FIX ENV] ${path.relative(appRoot, fp)}: ${dots}${seg} → ${relFixed}`);
        }
        return fixed;
      });

      if (changed) fs.writeFileSync(fp, content);
    }
  }
}

walk(featuresRoot);
console.log('Done fixing imports!');
