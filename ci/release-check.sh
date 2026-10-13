#!/usr/bin/env bash
#
# Checks that HEAD can be released as VERSION. Run before pushing the release
# tag, and by the release workflow before building. See RELEASING.md.
#
# Usage: ci/release-check.sh VERSION
#        ci/release-check.sh --remote
#
# VERSION: runs the checks, prints each failure and the warnings. Exit 0 when
# releasable, 1 on a failed check, 2 without VERSION. A failing git command
# ends the run with its own status.
#
# --remote: prints the release remote name, no checks. Exit 1 with the reason
# on stderr when there is none.
#
# The release remote is the one with a push URL at
# github.com/geoserver/geoserver-cloud; a fork named origin does not count.
# GIT_REMOTE picks one by name, and it must point there too.

set -euo pipefail

# Paths below are relative to the repository root. CDPATH could redirect the cd.
unset CDPATH
cd "$(dirname "$0")/.."

# The release repository. is_release_repository matches the same name in URLs.
release_repository=github.com/geoserver/geoserver-cloud
fork_pom=geoserver_submodule/geoserver/src/pom.xml

failures=()
warnings=()

# Set by the checks, printed by the summary.
version=""
revision=""
remote=""
remote_url=""
gs_version=""
gt_version=""
fork_version=""
fork_gt_version=""
submodule_commit=""
submodule_branch=""

# Why resolve_remote found no release remote.
remote_error=""

# The commit at HEAD, read once before the checks.
head_commit=""

# The commit behind tag VERSION on the remote, empty when the remote has no such
# tag. Looked up once by check_remote_tag, read by check_head_is_branch_head.
remote_tag=""

main() {
  if [ "${1:-}" = "--remote" ]; then
    print_remote
    return
  fi
  read_version_argument "$@"
  read_head_commit
  check_version_format
  check_series
  check_tag_and_branch_names
  check_head_is_branch_head
  check_working_tree
  check_submodule
  check_fork_versions
  check_warnings
  print_summary
  print_verdict
}

# Prints the name alone, other steps capture it. Failures go to stderr.
print_remote() {
  resolve_remote
  if [ -n "$remote_error" ]; then
    echo "$remote_error" >&2
    exit 1
  fi
  echo "$remote"
}

read_version_argument() {
  version="${1:-}"
  if [ -z "$version" ]; then
    echo "usage: $0 VERSION | --remote" >&2
    exit 2
  fi
}

read_head_commit() {
  head_commit=$(git rev-parse HEAD)
}

# x.y.z, x.y.z.n (GeoServer Cloud patch), x.y.z-Mn, x.y.z-RC or x.y.z-RCn. No
# patch number together with a suffix.
check_version_format() {
  if [[ ! "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+(\.[0-9]+|-(M[0-9]+|RC[0-9]*))?$ ]]; then
    fail "version '$version' is not of the form x.y.z, x.y.z.n, x.y.z-Mn, x.y.z-RC or x.y.z-RCn"
  fi
}

# A branch at 3.1.1-SNAPSHOT releases 3.1.x versions only.
check_series() {
  revision=$(pom_value pom.xml revision)
  if [ "$(series "$version")" != "$(series "$revision")" ]; then
    fail "version $version is not in the series of this branch, whose revision is $revision"
  fi
}

series() {
  echo "$1" | cut -d . -f 1,2
}

# The tag must be absent, or on HEAD as in the tag build. A branch of the same
# name would make the name ambiguous.
check_tag_and_branch_names() {
  check_local_names
  check_remote_names
}

check_local_names() {
  local local_tag
  if local_tag=$(git rev-parse -q --verify "refs/tags/$version^{commit}"); then
    if [ "$local_tag" != "$head_commit" ]; then
      fail "tag $version exists locally and does not point at HEAD"
    fi
  fi
  if git rev-parse -q --verify "refs/heads/$version" > /dev/null; then
    fail "a local branch named $version exists; the tag name would be ambiguous"
  fi
}

check_remote_names() {
  resolve_remote
  if [ -n "$remote_error" ]; then
    fail "$remote_error"
  else
    check_remote_tag
    check_remote_branch
  fi
}

# Sets remote and remote_url, or remote_error. No exit status on purpose: an if
# condition runs without set -e and would hide a failing git.
resolve_remote() {
  remote=""
  remote_url=""
  remote_error=""
  if [ -n "${GIT_REMOTE:-}" ]; then
    resolve_named_remote "$GIT_REMOTE"
  else
    resolve_remote_by_url
  fi
}

resolve_named_remote() {
  local name="$1"
  local url
  if ! url=$(git remote get-url --push "$name" 2>/dev/null); then
    remote_error="git remote '$name' not found"
  elif is_release_repository "$url"; then
    remote="$name"
    remote_url="$url"
  else
    remote_error="remote '$name' points at $url, not at $release_repository"
  fi
}

# Without GIT_REMOTE, exactly one remote must qualify.
resolve_remote_by_url() {
  local names name url
  local listing=""
  local match_names=() match_urls=()
  names=$(git remote)
  for name in $names; do
    url=$(git remote get-url --push "$name")
    listing="$listing"$'\n'"  $name  $url"
    if is_release_repository "$url"; then
      match_names+=("$name")
      match_urls+=("$url")
    fi
  done
  case "${#match_names[@]}" in
    0)
      remote_error="no remote points at $release_repository; add one$listing"
      ;;
    1)
      remote="${match_names[0]}"
      remote_url="${match_urls[0]}"
      ;;
    *)
      remote_error="several remotes point at $release_repository: $(join_with_commas "${match_names[@]}"); set GIT_REMOTE"
      ;;
  esac
}

join_with_commas() {
  local joined="" separator="" item
  for item in "$@"; do
    joined="$joined$separator$item"
    separator=", "
  done
  echo "$joined"
}

# Whether URL is a github.com URL (scp-like, ssh or https) of the release
# repository. A trailing .git or slash is fine, and the case is ignored as on
# GitHub. The host must be github.com itself.
is_release_repository() {
  local url
  url=$(printf '%s' "$1" | tr '[:upper:]' '[:lower:]')
  [[ "$url" =~ (^|[@/])github\.com[:/]geoserver/geoserver-cloud(\.git)?/?$ ]]
}

# The only lookup of the remote tag: check_head_is_branch_head reads the result.
check_remote_tag() {
  remote_tag=$(remote_tag_commit)
  if [ -n "$remote_tag" ] && [ "$remote_tag" != "$head_commit" ]; then
    fail "tag $version exists on $remote and does not point at HEAD"
  fi
}

# The commit behind the remote tag, or nothing. An annotated tag lists its
# commit as refs/tags/NAME^{}; a lightweight tag is the commit itself. Called in
# a command substitution, where set -e does not apply: without the return a
# failing git would read as a missing tag.
remote_tag_commit() {
  local listing commit
  listing=$(git ls-remote --tags "$remote" "refs/tags/$version" "refs/tags/$version^{}") || return
  commit=$(echo "$listing" | awk -v ref="refs/tags/$version^{}" '$2 == ref {print $1}')
  if [ -z "$commit" ]; then
    commit=$(echo "$listing" | awk -v ref="refs/tags/$version" '$2 == ref {print $1}')
  fi
  echo "$commit"
}

check_remote_branch() {
  local remote_branch
  remote_branch=$(git ls-remote --heads "$remote" "refs/heads/$version")
  if [ -n "$remote_branch" ]; then
    fail "a branch named $version exists on $remote; the tag name would be ambiguous"
  fi
}

# A tag on a commit outside the remote branches would release history missing
# from all of them. The tag build is exempt: the tag already fixes the commit.
check_head_is_branch_head() {
  local branch_heads
  if [ -z "$remote" ]; then
    return
  fi
  if [ -n "$remote_tag" ]; then
    return
  fi
  branch_heads=$(remote_branch_heads)
  if ! grep -qxF "$head_commit" <<< "$branch_heads"; then
    fail "HEAD is not the head of a branch on $remote; push it, or pull, before tagging"
  fi
}

remote_branch_heads() {
  git ls-remote --heads "$remote" | cut -f 1
}

# Changes inside the submodules count as well.
check_working_tree() {
  local uncommitted
  uncommitted=$(git status --porcelain --ignore-submodules=none)
  if [ -n "$uncommitted" ]; then
    fail "the working tree has uncommitted or untracked changes, see git status"
  fi
}

# The submodule must be initialized and at the recorded commit.
check_submodule() {
  local submodule_status
  submodule_status=$(git submodule status -- geoserver_submodule/geoserver)
  case "$submodule_status" in
    " "*) ;;
    "-"*) fail "the GeoServer submodule is not initialized: git submodule update --init" ;;
    "+"*) fail "the GeoServer submodule is not at the commit recorded in the tree" ;;
    *) fail "unexpected GeoServer submodule state: $submodule_status" ;;
  esac
  record_submodule_pin
}

# The recorded commit and the branch named in .gitmodules, for the summary.
record_submodule_pin() {
  submodule_commit=$(git rev-parse "HEAD:geoserver_submodule/geoserver")
  submodule_branch=$(git config -f .gitmodules submodule.geoserver.branch || echo "(none)")
}

# gs.version and gt.version must match what the submodule declares: the build
# resolves them from its artifacts.
check_fork_versions() {
  gs_version=$(pom_value src/pom.xml gs.version)
  gt_version=$(pom_value src/pom.xml gt.version)
  fork_version="(no submodule checkout)"
  fork_gt_version="$fork_version"
  if [ -f "$fork_pom" ]; then
    fork_version=$(pom_value "$fork_pom" version)
    fork_gt_version=$(pom_value "$fork_pom" gt.version)
    if [ "$fork_version" != "$gs_version" ]; then
      fail "gs.version $gs_version differs from the submodule's GeoServer version $fork_version"
    fi
    if [ "$fork_gt_version" != "$gt_version" ]; then
      fail "gt.version $gt_version differs from the submodule's gt.version $fork_gt_version"
    fi
  fi
}

# Snapshot upstream versions only warn: normal for milestones, and patch
# releases often ship on GeoTools snapshots.
check_warnings() {
  case "$gs_version" in
    *-SNAPSHOT) warn "gs.version $gs_version is a snapshot" ;;
  esac
  case "$gt_version" in
    *-SNAPSHOT) warn "gt.version $gt_version is a snapshot" ;;
  esac
  if [[ ! "$version" =~ -(M|RC) ]] && [[ "$submodule_branch" == */integration ]]; then
    warn "final release while .gitmodules tracks $submodule_branch; a fork release branch is expected"
  fi
}

print_summary() {
  local repository warning release_remote="(none)"
  if [ -n "$remote" ]; then
    release_remote="$remote ($remote_url)"
  fi
  repository=$(sed -n 's|^REPOSITORY=||p' docker-build/.env)
  echo "Release check for $version"
  echo "  revision:    $revision"
  echo "  gs.version:  $gs_version (submodule: $fork_version)"
  echo "  gt.version:  $gt_version (submodule: $fork_gt_version)"
  echo "  submodule:   $submodule_commit, tracking $submodule_branch"
  echo "  remote:      $release_remote"
  echo "  images:      $repository/<image>:$version"
  for warning in ${warnings[@]+"${warnings[@]}"}; do
    echo "WARNING: $warning"
  done
}

print_verdict() {
  local failure
  if [ "${#failures[@]}" -gt 0 ]; then
    for failure in ${failures[@]+"${failures[@]}"}; do
      echo "FAIL: $failure"
    done
    exit 1
  fi
  echo "OK: $version can be released from $(git rev-parse --short HEAD)"
}

fail() {
  failures+=("$1")
}

warn() {
  warnings+=("$1")
}

# The first <ELEMENT> in FILE, read with sed: the poms hold these values as
# one-line literals. A second sed instead of head -n 1: head exits early, and
# under pipefail the first sed then dies of SIGPIPE on a long pom, silently.
pom_value() {
  local file="$1" element="$2"
  sed -n "s|^ *<$element>\(.*\)</$element>.*|\1|p" "$file" | sed -n 1p
}

main "$@"
