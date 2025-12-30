#!/bin/bash
# Update all dependencies except tailwindcss (major and minor)
ncu -u --reject tailwindcss
# Update tailwindcss only within v3 (minor)
ncu -u --target minor --filter tailwindcss
