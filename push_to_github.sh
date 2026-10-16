#!/usr/bin/env bash
set -e

REPO_NAME="${1:-PDFMaster}"
VISIBILITY="${2:---public}"

echo "🚀 Creating and pushing to GitHub repository: $REPO_NAME..."

# Create repo and push
gh repo create "$REPO_NAME" $VISIBILITY --source=. --remote=origin --push

echo "✅ Successfully pushed to GitHub!"
echo "🌐 URL: https://github.com/$(gh api user -q .login)/$REPO_NAME"
