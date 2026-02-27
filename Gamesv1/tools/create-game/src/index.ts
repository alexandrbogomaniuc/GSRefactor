import { Command } from 'commander';
import fs from 'fs-extra';
import path from 'path';
import chalk from 'chalk';

const program = new Command();

program
    .name('create-game')
    .description('Scaffold a new slot game from the canonical template')
    .requiredOption('--name <name>', 'Name of the game (e.g., MyGame)')
    .requiredOption('--id <id>', 'Game ID (e.g., 1234)')
    .requiredOption('--slug <slug>', 'URL slug (e.g., my-game)')
    .action(async (options) => {
        const { name, id, slug } = options;
        const projectRoot = path.resolve(process.cwd());
        const templateDir = path.join(projectRoot, 'games', 'template-slot');
        const targetDir = path.join(projectRoot, 'games', slug);

        console.log(chalk.cyan(`🎰 GS Platform: Scaffolding new game '${name}' (ID: ${id})...`));

        try {
            if (await fs.pathExists(targetDir)) {
                console.error(chalk.red(`Error: Directory 'games/${slug}' already exists.`));
                process.exit(1);
            }

            // 1. Create directory structure
            await fs.ensureDir(targetDir);
            await fs.ensureDir(path.join(targetDir, 'assets'));
            await fs.ensureDir(path.join(targetDir, 'locales'));
            await fs.ensureDir(path.join(targetDir, 'gs'));

            // 2. Copy game.settings.json from template
            // Note: Assuming it exists in template-slot/src/game/config/game.settings.json
            const templateSettingsPath = path.join(templateDir, 'src', 'game', 'config', 'game.settings.json');
            const targetSettingsPath = path.join(targetDir, 'game.settings.json');

            if (await fs.pathExists(templateSettingsPath)) {
                const settings = await fs.readJson(templateSettingsPath);
                settings.gameId = id;
                settings.gameName = name;
                await fs.writeJson(targetSettingsPath, settings, { spaces: 2 });
                console.log(chalk.green('✔ game.settings.json created and updated.'));
            } else {
                // Create a default if not found
                await fs.writeJson(targetSettingsPath, { gameId: id, gameName: name, version: "1.0.0" }, { spaces: 2 });
                console.log(chalk.yellow('! Template game.settings.json not found, created default.'));
            }

            // 3. Create locales
            await fs.writeJson(path.join(targetDir, 'locales', 'en.json'), { "GAME_TITLE": name }, { spaces: 2 });

            // 4. Create a basic package.json for the game
            const gamePackageJson = {
                name: `@games/${slug}`,
                version: "1.0.0",
                private: true,
                type: "module",
                scripts: {
                    "dev": "vite",
                    "build": "tsc && vite build"
                },
                dependencies: {
                    "@gs/slot-shell": "workspace:*",
                    "@gs/protocol": "workspace:*",
                    "pixi.js": "^8.8.1"
                },
                devDependencies: {
                    "vite": "^6.2.0",
                    "typescript": "~5.7.3"
                }
            };
            await fs.writeJson(path.join(targetDir, 'package.json'), gamePackageJson, { spaces: 2 });

            console.log(chalk.green(`\n✅ Success! New game project created at: games/${slug}`));
            console.log(chalk.white('Next steps:'));
            console.log(chalk.cyan(`  pnpm install`));
            console.log(chalk.cyan(`  cd games/${slug}`));
            console.log(chalk.cyan(`  pnpm dev`));

        } catch (err) {
            console.error(chalk.red('An error occurred during scaffolding:'), err);
            process.exit(1);
        }
    });

program.parse();
