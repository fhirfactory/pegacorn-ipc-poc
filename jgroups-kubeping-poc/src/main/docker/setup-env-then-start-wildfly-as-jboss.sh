#!/usr/bin/env bash
# NOTE: this file should have Unix (LF) EOL conversion performed on it to avoid: "env: can't execute 'bash ': No such file or directory"

echo "Staring setup-env-then-start-wildfly-as-jboss.sh as user $(whoami) with params $@"

echo "DOCKER IMAGE_BUILD_TIMESTAMP=${IMAGE_BUILD_TIMESTAMP}"
echo "HELM_RELEASE_TIME=${HELM_RELEASE_TIME}"

addgroup -g 5000 -S workaround
addgroup jboss workaround

# then start /start-wildfly.sh script as jboss user
# NOTE: gosu is used instead of su-exec as the wildfly docker image is based on centos, whereas the postgres one is based on alpine,
# and the Alpine su-exec program is a substitute for gosu (see https://devops.stackexchange.com/a/5242 and
# https://github.com/docker-library/postgres/blob/33bccfcaddd0679f55ee1028c012d26cd196537d/12/docker-entrypoint.sh line 281 vs
# https://github.com/docker-library/postgres/blob/33bccfcaddd0679f55ee1028c012d26cd196537d/12/alpine/docker-entrypoint.sh line 281
exec gosu jboss "/start-wildfly.sh" "$@"