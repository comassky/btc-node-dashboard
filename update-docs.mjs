#!/usr/bin/env node

/**
 * Script to automatically update documentation with versions from pom.xml and package.json
 * Usage: node update-docs.mjs
 */

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

// Read package.json
const packageJsonPath = path.join(__dirname, 'src/main/web/package.json');
const packageJson = JSON.parse(fs.readFileSync(packageJsonPath, 'utf-8'));

// Read pom.xml
const pomPath = path.join(__dirname, 'pom.xml');
const pomXml = fs.readFileSync(pomPath, 'utf-8');

// Read tsconfig.json
const tsconfigPath = path.join(__dirname, 'src/main/web/tsconfig.json');
const tsconfigContent = fs.readFileSync(tsconfigPath, 'utf-8');
const targetMatch = tsconfigContent.match(/"target"\s*:\s*"([^"]+)"/);
const typescriptTarget = targetMatch ? targetMatch[1] : 'ES2020';

// Extract versions from pom.xml
const extractPomVersion = (tag) => {
  const regex = new RegExp(`<${tag}>([^<]+)</${tag}>`);
  const match = pomXml.match(regex);
  return match ? match[1] : null;
};

const extractProperty = (name) => {
  const regex = new RegExp(`<${name}>([^<]+)</${name}>`);
  const match = pomXml.match(regex);
  return match ? match[1] : null;
};

// Backend versions
const backendVersions = {
  projectVersion: extractPomVersion('version'),
  javaVersion: extractProperty('java.version') || '25',
  javaRelease: extractProperty('maven.compiler.release') || '25',
  quarkusVersion: extractProperty('quarkus.platform.version') || '3.30.5',
  mavenVersion: extractProperty('maven.version') || '3.9.11',
  nodeVersion: extractProperty('node.version') || 'v24.12.0',
  pnpmVersion: extractProperty('pnpm.version') || '10.27.0',
  npmVersion: extractProperty('npm.version') || '11.6.2',
  // Maven plugins
  mavenCompilerPlugin: extractProperty('maven-compiler-plugin.version') || '3.14.1',
  mavenSurefirePlugin: extractProperty('maven-surefire-plugin.version') || '3.5.4',
  mavenFailsafePlugin: extractProperty('maven-failsafe-plugin.version') || '3.5.4',
  frontendMavenPlugin: extractProperty('frontend-maven-plugin.version') || '2.0.0',
};

// Frontend versions from package.json
const frontendVersions = {
  // Runtime dependencies
  vue: packageJson.dependencies?.vue || '3.5.26',
  vueuse: packageJson.dependencies?.['@vueuse/core'] || '14.1.0',
  pinia: packageJson.dependencies?.pinia || '3.0.4',
  chartjs: packageJson.dependencies?.['chart.js'] || '4.5.1',
  floatingUi: packageJson.dependencies?.['@floating-ui/vue'] || '1.1.9',
  dateFns: packageJson.dependencies?.['date-fns'] || '4.1.0',
  filesize: packageJson.dependencies?.filesize || '11.0.13',
  
  // Dev dependencies
  typescript: packageJson.devDependencies?.typescript || '5.9.3',
  typescriptTarget: typescriptTarget,
  vite: packageJson.devDependencies?.vite || '7.3.0',
  tailwind: packageJson.devDependencies?.tailwindcss || '4.1.18',
  vitest: packageJson.devDependencies?.vitest || '4.0.16',
  prettier: packageJson.devDependencies?.prettier || '3.7.4',
  iconifyFaBrands: packageJson.devDependencies?.['@iconify-json/fa6-brands']?.replace('^', '') || '1.2.6',
  iconifySimpleIcons: packageJson.devDependencies?.['@iconify-json/simple-icons']?.replace('^', '') || '1.2.65',
};

console.log('📦 Extracted Versions:');
console.log('Backend:', backendVersions);
console.log('Frontend:', frontendVersions);

// Update README.md
const updateReadme = () => {
  const readmePath = path.join(__dirname, 'README.md');
  let readme = fs.readFileSync(readmePath, 'utf-8');
  
  // Update badges
  readme = readme.replace(
    /!\[Java\]\(https:\/\/img\.shields\.io\/badge\/Java-\d+-orange\.svg\)/,
    `![Java](https://img.shields.io/badge/Java-${backendVersions.javaVersion}-orange.svg)`
  );
  
  readme = readme.replace(
    /!\[Quarkus\]\(https:\/\/img\.shields\.io\/badge\/Quarkus-[\d.]+-blue\.svg\)/,
    `![Quarkus](https://img.shields.io/badge/Quarkus-${backendVersions.quarkusVersion}-blue.svg)`
  );
  
  readme = readme.replace(
    /!\[Vue\]\(https:\/\/img\.shields\.io\/badge\/Vue\.js-[\d.]+-green\.svg\)/,
    `![Vue](https://img.shields.io/badge/Vue.js-${frontendVersions.vue}-green.svg)`
  );
  
  readme = readme.replace(
    /!\[TypeScript\]\(https:\/\/img\.shields\.io\/badge\/TypeScript-[\d.]+-blue\.svg\)/,
    `![TypeScript](https://img.shields.io/badge/TypeScript-${frontendVersions.typescript}-blue.svg)`
  );
  
  // Update Backend table
  readme = readme.replace(
    /(^\| \*\*Java\*\*\s+\| )\d+(\s+\|)/m,
    `$1${backendVersions.javaVersion}$2`
  );
  
  readme = readme.replace(
    /(^\| \*\*Quarkus\*\*\s+\| )[\d.]+(\s+\|)/m,
    `$1${backendVersions.quarkusVersion}$2`
  );
  
  // Update Frontend Runtime Dependencies table
  const runtimeDeps = [
    ['Vue.js', frontendVersions.vue],
    ['VueUse', frontendVersions.vueuse],
    ['Pinia', frontendVersions.pinia],
    ['Chart.js', frontendVersions.chartjs],
    ['Floating UI', frontendVersions.floatingUi],
    ['date-fns', frontendVersions.dateFns],
    ['filesize', frontendVersions.filesize],
  ];
  
  runtimeDeps.forEach(([name, version]) => {
    const escapedName = name.replace(/\./g, '\\.');
    const regex = new RegExp(`(^\\| \\*\\*${escapedName}\\*\\*\\s+\\| )[\\d.]+( \\|)`, 'gm');
    readme = readme.replace(regex, `$1${version}$2`);
  });
  
  // Update Frontend Build Tools table
  const buildTools = [
    ['TypeScript', frontendVersions.typescript],
    ['Vite', frontendVersions.vite],
    ['Tailwind CSS', frontendVersions.tailwind],
    ['Vitest', frontendVersions.vitest],
    ['Prettier', frontendVersions.prettier],
  ];
  
  buildTools.forEach(([name, version]) => {
    const escapedName = name.replace(/\./g, '\\.');
    const regex = new RegExp(`(^\\| \\*\\*${escapedName}\\*\\*\\s+\\| )[\\d.]+( \\|)`, 'gm');
    readme = readme.replace(regex, `$1${version}$2`);
  });
  
  // Update Build & Deploy section
  readme = readme.replace(
    /\*\*Maven\*\* [\d.]+ /,
    `**Maven** ${backendVersions.mavenVersion} `
  );
  
  readme = readme.replace(
    /\*\*Node\.js\*\* v[\d.]+ /,
    `**Node.js** ${backendVersions.nodeVersion} `
  );
  
  readme = readme.replace(
    /\*\*pnpm\*\* [\d.]+ /,
    `**pnpm** ${backendVersions.pnpmVersion} `
  );
  
  readme = readme.replace(
    /\*\*npm\*\* [\d.]+ /,
    `**npm** ${backendVersions.npmVersion} `
  );
  
  // Update version in docker tags
  readme = readme.replace(
    /- Current version: `[\d.]+-SNAPSHOT`/,
    `- Current version: \`${backendVersions.projectVersion}\``
  );
  
  // Update Maven plugins in Build & Deploy section
  readme = readme.replace(
    /\*\*Maven Compiler Plugin\*\* [\d.]+/,
    `**Maven Compiler Plugin** ${backendVersions.mavenCompilerPlugin}`
  );
  
  readme = readme.replace(
    /\*\*Maven Surefire Plugin\*\* [\d.]+/,
    `**Maven Surefire Plugin** ${backendVersions.mavenSurefirePlugin}`
  );
  
  readme = readme.replace(
    /\*\*Maven Failsafe Plugin\*\* [\d.]+/,
    `**Maven Failsafe Plugin** ${backendVersions.mavenFailsafePlugin}`
  );
  
  readme = readme.replace(
    /\*\*Frontend Maven Plugin\*\* [\d.]+/,
    `**Frontend Maven Plugin** ${backendVersions.frontendMavenPlugin}`
  );
  
  fs.writeFileSync(readmePath, readme, 'utf-8');
  console.log('✅ Updated README.md');
};

// Update TESTING.md
const updateTesting = () => {
  const testingPath = path.join(__dirname, 'TESTING.md');
  let testing = fs.readFileSync(testingPath, 'utf-8');
  
  // Update Frontend technologies
  const techVersions = [
    ['Vitest', frontendVersions.vitest],
    ['Vue Test Utils', '2.4.6'],
    ['Happy DOM', '20.0.11'],
    ['Vite', frontendVersions.vite],
    ['TypeScript', frontendVersions.typescript],
    ['VueUse', frontendVersions.vueuse],
    ['Chart.js', frontendVersions.chartjs],
    ['Tailwind CSS', frontendVersions.tailwind],
    ['Iconify', '5.0.0'],
    ['Simple Icons', frontendVersions.iconifySimpleIcons],
    ['Floating UI', frontendVersions.floatingUi],
  ];
  
  techVersions.forEach(([name, version]) => {
    const escapedName = name.replace(/\./g, '\\.');
    const regex = new RegExp(`${escapedName} \\([\\d.]+\\)`, 'g');
    testing = testing.replace(regex, `${name} (${version})`);
  });
  
  // Update pnpm version
  testing = testing.replace(
    /\*\*pnpm\*\* [\d.]+ /,
    `**pnpm** ${backendVersions.pnpmVersion} `
  );
  
  fs.writeFileSync(testingPath, testing, 'utf-8');
  console.log('✅ Updated TESTING.md');
};

// Update CONTRIBUTING.md
const updateContributing = () => {
  const contributingPath = path.join(__dirname, 'CONTRIBUTING.md');
  let contributing = fs.readFileSync(contributingPath, 'utf-8');
  
  // Update versions section
  contributing = contributing.replace(
    /- Java \d+/,
    `- Java ${backendVersions.javaVersion}`
  );
  
  contributing = contributing.replace(
    /- Quarkus [\d.]+/,
    `- Quarkus ${backendVersions.quarkusVersion}`
  );
  
  contributing = contributing.replace(
    /- Node\.js v[\d.]+/,
    `- Node.js ${backendVersions.nodeVersion}`
  );
  
  contributing = contributing.replace(
    /- pnpm [\d.]+/,
    `- pnpm ${backendVersions.pnpmVersion}`
  );
  
  contributing = contributing.replace(
    /- npm [\d.]+/,
    `- npm ${backendVersions.npmVersion}`
  );
  
  fs.writeFileSync(contributingPath, contributing, 'utf-8');
  console.log('✅ Updated CONTRIBUTING.md');
};

// Update BUILD.md
const updateBuild = () => {
  const buildPath = path.join(__dirname, 'BUILD.md');
  let build = fs.readFileSync(buildPath, 'utf-8');
  
  // Update prerequisites
  build = build.replace(
    /\*\*Required:\*\* Java \d+\+, Maven [\d.]+\+/,
    `**Required:** Java ${backendVersions.javaVersion}+, Maven ${backendVersions.mavenVersion}+`
  );
  
  build = build.replace(
    /\*\*Optional:\*\* Node\.js \d+\+ \(v[\d.]+/,
    `**Optional:** Node.js 24+ (${backendVersions.nodeVersion}`
  );
  
  build = build.replace(
    /pnpm [\d.]+/g,
    `pnpm ${backendVersions.pnpmVersion}`
  );
  
  build = build.replace(
    /npm [\d.]+/g,
    `npm ${backendVersions.npmVersion}`
  );
  
  fs.writeFileSync(buildPath, build, 'utf-8');
  console.log('✅ Updated BUILD.md');
};

// Update GitHub Actions workflows
const updateWorkflows = () => {
  const workflowFiles = [
    '.github/workflows/docker-dev-native.yml',
    '.github/workflows/docker-native.yml',
  ];

  workflowFiles.forEach((filePath) => {
    const fullPath = path.join(__dirname, filePath);
    if (!fs.existsSync(fullPath)) return;

    let workflow = fs.readFileSync(fullPath, 'utf-8');

    // Update GRAALVM_VERSION
    workflow = workflow.replace(
      /GRAALVM_VERSION: "\d+"/,
      `GRAALVM_VERSION: "${backendVersions.javaVersion}"`
    );

    // Update NODE_VERSION
    workflow = workflow.replace(
      /NODE_VERSION: "v?[\d.]+"/,
      `NODE_VERSION: "${backendVersions.nodeVersion}"`
    );

    // Update PNPM_VERSION
    workflow = workflow.replace(
      /PNPM_VERSION: "[\d.]+"/,
      `PNPM_VERSION: "${backendVersions.pnpmVersion}"`
    );

    // Update MAVEN_VERSION
    workflow = workflow.replace(
      /MAVEN_VERSION: "[\d.]+"/,
      `MAVEN_VERSION: "${backendVersions.mavenVersion}"`
    );

    fs.writeFileSync(fullPath, workflow, 'utf-8');
    console.log(`✅ Updated ${filePath}`);
  });
};

// Run all updates
try {
  updateReadme();
  updateTesting();
  updateContributing();
  updateBuild();
  updateWorkflows();
  console.log('\n✨ Documentation successfully updated with current versions!');
} catch (error) {
  console.error('❌ Error updating documentation:', error);
  process.exit(1);
}
