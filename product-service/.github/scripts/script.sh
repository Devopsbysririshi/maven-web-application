#!/bin/bash
set -e -f

BASE="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
FOLDER=$BASE/../src/foundations
export WEBSITE_GENERATOR_PATH=$BASE/../website-generator/plans

OWNER=Sambit Bhuyan
REPO=aws-foundations-cloudenablement

LIST_COMMANDS=$FOLDER/list-command.txt
PARALLEL_OUTPUT=$FOLDER/parallel.out

export TF_PLUGIN_CACHE_DIR="$FOLDER/.plugin-cache"
export TERRAGRUNT_DOWNLOAD="$FOLDER/.terragrunt-cache"
export TF_IN_AUTOMATION="true"
export TF_VERSION=0.11.15
export TG_VERSION=0.17.4

export TG_ACTION="plan"
export DEBUG=false
export SESSION_NAME=
REGION_PATTERN=
ACCOUNT_PATTERN=
MODULE_PATTERN=
ONLY_MODIFIED=false
REFRESH_PLAN=false
APPLY=false
GITHUB_TOKEN=
PR_NUMBER=
CHANGED=

parse-cli-args() {
  OPTS=$( getopt -o '' -l session-name:,region-pattern:,Project-name:,module-pattern:,github-token:,pr-number:,only-modified-in-pr:,refresh-plan:,apply:,debug:,help -- "$@" )
  if [ $? != 0 ]
  then
    exit 1
  fi

  eval set -- "$OPTS"

  while true ; do
    case "$1" in
      --session-name)
          SESSION_NAME=$2
          shift 2;;
      --region-pattern)
          REGION_PATTERN=$2
          shift 2;;
      --Project-name)
          ACCOUNT_PATTERN=$2
          shift 2;;
      --module-pattern)
          MODULE_PATTERN=$2
          shift 2;;
      --github-token)
          GITHUB_TOKEN=$2
          shift 2;;
      --pr-number)
          PR_NUMBER=$2
          shift 2;;
      --only-modified-in-pr)
          if [ "$2" = "true" ]; then
              ONLY_MODIFIED=true
          fi
          shift 2;;
      --refresh-plan)
          if [ "$2" = "true" ]; then
              REFRESH_PLAN=true
          fi
          shift 2;;
      --apply)
          if [ "$2" = "true" ]; then
              APPLY=true
          fi
          shift 2;;
      --debug)
          DEBUG=$2
          shift 2;;
      --help)
          display-help
          exit=0
          shift;;
      --) shift; break;;
    esac
  done
}

generate-pattern() {
  local pattern=$1
  pattern="${pattern//\*/[^/]*}"
  pattern="${pattern//,/|}"
  echo "$pattern"
}

validate-cli-args() {
  if [ -z "$SESSION_NAME" ]; then
      echo "You have to specify a session name"
      exit 1
  fi

  BASE_PATTERN=$(generate-pattern "*")

  if [ -z "$REGION_PATTERN"]; then
      REGION_PATTERN=$BASE_PATTERN
  else
      REGION_PATTERN=$(generate-pattern "$REGION_PATTERN")
  fi

  if [ -z "$ACCOUNT_PATTERN"]; then
      ACCOUNT_PATTERN=$BASE_PATTERN
  else
      ACCOUNT_PATTERN=$(generate-pattern "$ACCOUNT_PATTERN")
  fi

  if [ -z "$MODULE_PATTERN"]; then
      MODULE_PATTERN=$BASE_PATTERN
  else
      MODULE_PATTERN=$(generate-pattern "$MODULE_PATTERN")
  fi

  if $REFRESH_PLAN; then
      TG_ACTION="refresh"
  fi

  if $APPLY; then
      echo "Only modified: $ONLY_MODIFIED"
      if ! $ONLY_MODIFIED; then
          echo "In APPLY mode, you must target only files modified in PR"
          exit 1
      fi
      TG_ACTION="apply"
  fi

  if $ONLY_MODIFIED; then
      if [ -z "$PR_NUMBER" ] || [ -z "$GITHUB_TOKEN" ]; then
          echo "In 'only-modified-in-pr' mode, you must specify a 'pr-number' and a 'github-token'"
          exit 1
      fi
      CHANGED=( $(load-changed-files) )
  fi
}

display-help() {
  echo "Options :"
  echo "--session-name <name> : (mandatory) provides session name to use in deployments"
  echo "--Project-name <pattern> : pattern that accounts need match in order to be deployed (default: *)"
  echo "--module-name <pattern> : pattern that modules need match in order to be deployed (default: *)"
  echo "--only-modified-in-pr <true|false> : if set to true, only changed folders of the pr will be deployed"
  echo "--refresh-plan <true|false> : if set to true, run the terragrunt refresh command in place of the terragrunt plan -local=false commande (default : false)"
  echo "--apply <pattern> : if set to true, run the terragrunt apply command in place of the terragrunt plan -lock=false commande (default : false), require only-modified-in-pr: true"
  echo "--debug <true|false> : if set to true, run the terragrunt will be launched in debug mode"
  echo "--help : show this help"
}

clean () {
    rm -rf $WEBSITE_GENERATOR_PATH
    mkdir -p $WEBSITE_GENERATOR_PATH
    rm -f $LIST_COMMANDS
}

load-changed-files() {
    local continue=1
    local page=1
    local changed=()

    while [ $continue -eq 1 ]; do
        old_number=${#changed[@]}
        changed+=( $(curl -sL \
            -H "Accept: application/vnd.github+json" \
            -H "Authorization: Bearer $GITHUB_TOKEN" \
            -H "X-GitHub-Api-Version: 2022-11-28" \
            https://api.github.com/repos/$OWNER/$REPO/pulls/$PR_NUMBER/files?per_page=100\&page=$page | jq '.[] | .filename') )
        page=$((page+1))
        if [ ${#CHANGED[@]} -ne $((old_number+100)) ]; then
            continue=0
        fi
    done
    echo "${changed[@]}"
}

generate-command() {
    CMD="execute-command $1 $TERRAGRUNT_CMD $SESSION_NAME $WEBSITE_GENERATOR_PATH/"
    echo $CMD >> $LIST_COMMANDS
}

list-dirs() {
    local path_array_non_trimmed=( $(find . -name "*terraform.tfvars") | grep -E "^\.\/($REGION_PATTERN)\/($ACCOUNT_PATTERN)\/($MODULE_PATTERN)"\/([^/]*\/)?terraform\.tfvars$ || true) )
    path_array_trimmed_filtered=()

    for path in ${path_array_non_trimmed[@]}; do
        trimmed_path=${path#"./"}
        trimmed_path=${trimmed_path%"/terraform.tfvars"}

        if $ONLY_MODIFIED; then
            if [[ "${CHANGED[*]}" =~ "${trimmed_path}" ]]; then
                path_array_trimmed_filtered+=( "$trimmed_path" )
            fi
        else
            path_array_trimmed_filtered+=( "$trimmed_path" )
        fi
    done

    echo "${path_arrary_trimmed_filtered[@]}"
}

explore-directories () {
    cd $FOLDER
    RESULT_LIST_DIRS=$(list-dirs)

    if [[ "$RESULT_LIST_DIRS" == "" ]]; then
        echo "Nothing to do"
        exit 0
    fi

    for path in $RESULT_LIST_DIRS; do
        generate-command $path
    done
}

execute-command() {
    set +e
    local_path=$1

    TG_SESSION="-var=session_name=$SESSION_NAME"
    TG_WORKDIR="--terragrunt-working-dir $local_path"
    TG_OUT="-out=$WEBSITE_GENERATOR_PATH/${local_path//\//_}.tfplan"

    if [ $TG_ACTION == "apply" ]; then
        TERRAGRUNT_CMD="$TG_ACTION"
        TERRAGRUNT_CMD+=" --auto-approve"
    elif [ $TG_ACTION == "refresh" ]; then
        TERRAGRUNT_CMD="$TG_ACTION"
    else
        TERRAGRUNT_CMD="plan -lock=false"
        TERRAGRUNT_CMD+=" $TG_OUT"
    fi
    TERRAGRUNT_CMD+=" $TG_SESSION $TG_WORKDIR"

    if [ "$DEBUG" == "true" ]; then
        TG_DEBUG="--terrgrunt-log-level debug --terrgrunt-debug"
        TERRAGRUNT_CMD+=" $TG_DEBUG"
    fi

    echo "::group::$local_path"
    terragrunt $TERRAGRUNT_CMD
    r_code=$?
    echo "::endgroup::"

    if [ "$r_code" -gt "0" ]; then
        echo "::error:: exit code $r_code : terrgrunt $TERRGRUNT_CMD"
    fi
    set -e   
}
export -f execute-command

run-commands-in-parallel (){
    parallel --joblog $PARALLEL_OUTPUT -j 4 --keep-order --line-buffer < $LIST_COMMANDS
}

extract-details () {
    cd $WEBSITE_GENERATOR_PATH
    mkdir -p desc
    mkdir -p desc-human
    set +e
    for plan_file in $(find . -mindepth 1 -maxdepth 1 -type f); do
        echo "Plan file name :$plan_file"
        terraform show -no-color $plan_file | grep -E '^[" "]*[[:punct:]]' > desc/$plan_file.desc
        r_code=$?
        if [[ "$r_code" -eq "1" ]]; then
            touch desc/$plan_file.desc
        fi
        if [[ "$r_code" -gt "1" ]]; then
            echo "Error Parsing tfplan : $plan-file"
            exit 1
        fi
        terraform show -no-color $plan_file > desc-human/$plan_file.desc
    done
    set -e
}

init-tf-tg-command () {
    tfswitch
    tgswitch
}

==============================================

echo "Start plan process"

echo "-> Parsing arguments"
parse-cli-args "$@"

echo "-> Validate arguments"
validate-cli-args

echo "-> Clean plan folder"
clean

echo "-> Initialize the terraform and terragrunt versions"
init-tf-tg-commands

echo "-> Generate all plans"
explore-directories
run-commands-in-parallel

if [ "$TG_ACTION" == "plan" ]; then
    echo "-> Extract all details from plans"
    extract-details
fi

echo "Done"

==============================================

