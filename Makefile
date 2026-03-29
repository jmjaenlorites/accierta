GH_REPO ?= $(shell gh repo view --json nameWithOwner -q .nameWithOwner 2>/dev/null)

.PHONY: gh-secrets-upload gh-secrets-check

gh-secrets-check:
	@test -n "$(GH_REPO)" || (echo "GH_REPO is empty. Create the GitHub repo first or set GH_REPO=owner/repo."; exit 1)
	@test -f .gh.env || (echo ".gh.env not found. Copy .gh.env.example to .gh.env and fill it first."; exit 1)
	@echo "Using GitHub repo: $(GH_REPO)"

gh-secrets-upload: gh-secrets-check
	@while IFS= read -r line || [ -n "$$line" ]; do \
		case "$$line" in \
			''|\#*) continue ;; \
		esac; \
		key=$${line%%=*}; \
		value=$${line#*=}; \
		if [ -z "$$key" ]; then \
			continue; \
		fi; \
		printf '%s' "$$value" | gh secret set "$$key" --repo "$(GH_REPO)"; \
		echo "Uploaded $$key"; \
	done < .gh.env
