#!/bin/bash

SERVICE_NAME=$1
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

POSTGRES_ADMIN_PASSWORD=$(kubectl get secret --namespace default "${SERVICE_NAME}"-postgresql -o jsonpath="{.data.postgresql-postgres-password}" | base64 --decode)

echo "upgrading service $SERVICE_NAME with postgres_admin_password ${POSTGRES_ADMIN_PASSWORD} ..."

helm upgrade "${SERVICE_NAME}" "${SCRIPT_DIR}"/ecom-company/charts/"${SERVICE_NAME}" --set postgresqlPassword=${POSTGRES_ADMIN_PASSWORD}
