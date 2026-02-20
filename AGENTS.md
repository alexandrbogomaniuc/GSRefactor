## Skills
A skill is a set of local instructions to follow that is stored in a `SKILL.md` file. Below is the list of skills that can be used.

### Available skills
- project-continuity: Read project diary and latest launch-forensics first, then continue from last checkpoint. (file: /Users/alexb/.codex/skills/project-continuity/SKILL.md)
- skill-creator: Guide for creating effective skills. (file: /Users/alexb/.codex/skills/.system/skill-creator/SKILL.md)
- skill-installer: Install Codex skills from curated list or GitHub repo path. (file: /Users/alexb/.codex/skills/.system/skill-installer/SKILL.md)

### How to use skills
- Mandatory bootstrap: `project-continuity` must be used first for every request in `/Users/alexb/Documents/Dev`.
- Trigger rules:
  - Use a skill when user names it (`$SkillName` or plain text) OR task clearly matches description.
  - Multiple mentions mean use them all.
- Continuity first rule:
  - Before any action, read `/Users/alexb/Documents/Dev/docs/12-work-diary.md`.
  - For runtime/launch work, also read `/Users/alexb/Documents/Dev/docs/11-game-launch-forensics.md`.
- Missing/blocked:
  - If a named skill path cannot be read, state that briefly and continue with best fallback.
