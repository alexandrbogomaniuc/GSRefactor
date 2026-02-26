# M0 Baseline Evidence Pack

Generated at (UTC): 2026-02-26 09:15-09:17
Purpose: baseline lock before hard-cut namespace migration waves.

## Contents
- `git-head.txt` - commit hash used for baseline
- `git-status.txt` - repo status at capture time
- `docker-ps.txt` - running containers snapshot
- `refactor-gs-1-log-tail.txt` - GS log tail
- `refactor-mp-1-log-tail.txt` - MP log tail
- `refactor-gs-1-log-selected.txt` - GS filtered lines for migration-relevant tokens
- `refactor-mp-1-log-selected.txt` - MP filtered lines for migration-relevant tokens
- `gs-package-com-dgphoenix.txt` - all GS files with legacy package declarations
- `gs-package-com-abs.txt` - all GS files with target package declarations
- `mp-package-com-dgphoenix.txt` - all MP files with legacy package declarations
- `mp-package-com-abs.txt` - all MP files with target package declarations
- `gs-pom-groupid-com-dgphoenix.txt` - GS pom legacy groupId references
- `gs-pom-groupid-com-abs.txt` - GS pom target groupId references
- `mp-pom-groupid-com-dgphoenix.txt` - MP pom legacy groupId references
- `mp-pom-groupid-com-abs.txt` - MP pom target groupId references
- `runtime-classstring-inventory.txt` - GS runtime config/support class-string inventory
- `mp-runtime-token-inventory.txt` - MP runtime token inventory
- `gs-top-files-package-com-dgphoenix.txt` - GS top files by legacy package declaration
- `runtime-classstring-top-files.txt` - top runtime class-string hotspot files
- `mp-runtime-token-top-files.txt` - top MP runtime token hotspot files
- `baseline-counts.txt` - consolidated baseline numeric summary

## Notes
- This pack is immutable baseline evidence for M0.
- All rename waves must reference this pack in before/after comparisons.
