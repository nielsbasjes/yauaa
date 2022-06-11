#!/bin/bash

release_tag=main
sailr_repo="https://github.com/craicoverflow/sailr/tree/$release_tag"

SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

# checks that jq is usable
function check_jq_exists_and_executable {
if ! [ -x "$(command -v jq)" ]; then
  echo -e "\`commit-msg\` hook failed. Please install jq."
  exit 1
fi
}

# check if the config file exists
# if it doesnt we dont need to run the hook
function check_sailr_config {
  if [[ ! -f "$CONFIG" ]]; then
    echo -e "Sailr config file is missing. To set one see $sailr_repo#usage"
    exit 0
  fi
}

function set_config {
  local_config="$SCRIPTDIR/sailr.json"

  if [ -f "$local_config" ]; then
    CONFIG=$local_config
  elif [ -n "$SAILR_CONFIG" ]; then
    CONFIG=$SAILR_CONFIG
  fi
}

# set values from config file to variables
function set_config_values() {
  enabled=$(jq -r .enabled "$CONFIG")

  if [[ ! $enabled ]]; then
    exit 0
  fi

  revert=$(jq -r .revert "$CONFIG")
  types=($(jq -r '.types[]' "$CONFIG"))
  min_length=$(jq -r .length.min "$CONFIG")
  max_length=$(jq -r .length.max "$CONFIG")
}

# build the regex pattern based on the config file
function build_regex() {
  set_config_values

  regexp="^[.0-9]+$|"

  if $revert; then
      regexp="${regexp}^([Rr]elease|[Rr]evert|[Mm]erge):? )?.*$|^("
  fi

  for type in "${types[@]}"
  do
    regexp="${regexp}$type|"
  done

  regexp="${regexp%|})(\(.+\))?: "

  regexp="${regexp}.{$min_length,$max_length}$"
}


# Print out a standard error message which explains
# how the commit message should be structured
function print_error() {
  commit_message=$1
  regular_expression=$2
  echo -e "\n\e[31m[Invalid Commit Message]"
  echo -e "------------------------\033[0m\e[0m"
  echo -e "Valid types            : \e[36m${types[@]}\033[0m"
  echo -e "Max length (first line): \e[36m$max_length\033[0m"
  echo -e "Min length (first line): \e[36m$min_length\033[0m"
  echo -e "\e[37mRegex                  : \e[33m$regular_expression\033[0m\n"
  echo -e "\e[37mCommit message         : \e[33m\"$commit_message\"\033[0m"
  echo -e "\e[37mCommit message length  : \e[33m$(echo $commit_message | wc -c)\033[0m\n"
}

set_config

# check if the repo has a sailr config file
check_sailr_config

# make sure jq is installed
check_jq_exists_and_executable

# get the first line of the commit message
INPUT_FILE=$1
START_LINE=$(head -n1 $INPUT_FILE)

build_regex

if [[ ! $START_LINE =~ $regexp ]]; then
  # commit message is invalid according to config - block commit
  print_error "$START_LINE" "$regexp"
  exit 1
fi

pass "Valid commit message format: Ok"
