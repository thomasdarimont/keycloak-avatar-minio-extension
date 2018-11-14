# Simple extension for managing avatar images in Keycloak

This PoC extension exposes a custom realm resource to manage user avatars.
The avatar images are stored in a [minio](https://www.minio.io/) backend.

Avatar images can either be uploaded via the account page of the custom
account theme `account-avatar` or via the `AvatarResource` custom realm resource. 

<img src="keycloak-avatar-demo.png" alt="Keycloak Avatar Demo">

## Start the minio backend
```
docker run -p 9000:9000 --name keycloak-avatars \
  -e "MINIO_ACCESS_KEY=AKIAIOSFODNN7EXAMPLE" \
  -e "MINIO_SECRET_KEY=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY" \
  -v $PWD/data:/data \
  -v $PWD/config:/root/.minio \
  minio/minio server /data
```

## Build the example
```
mvn clean verify
```

## Deploy the example
Copy to the `standalone/deployments` directory in Keycloak.

## Uploading an avatar image via account theme
Configure the `avatar-account` theme as account theme in the realm settings.

Open the account page for a user and select and upload an image.

## Uploading an avatar image via CURL

Retrieve access token
```
KC_USERNAME=tester
KC_PASSWORD=test
KC_CLIENT=admin-cli
KC_CLIENT_SECRET=""
KC_REALM=avatar-demo
KC_URL=http://localhost:8080/auth
KC_RESPONSE=$( \
   curl -k \
        -d "username=$KC_USERNAME" \
        -d "password=$KC_PASSWORD" \
        -d 'grant_type=password' \
        -d "client_id=$KC_CLIENT" \
        -d "client_secret=$KC_CLIENT_SECRET" \
        "$KC_URL/realms/$KC_REALM/protocol/openid-connect/token" \
    | jq .
)

KC_ACCESS_TOKEN=$(echo $KC_RESPONSE| jq -r .access_token)
KC_ID_TOKEN=$(echo $KC_RESPONSE| jq -r .id_token)
KC_REFRESH_TOKEN=$(echo $KC_RESPONSE| jq -r .refresh_token)
```

## Upload avatar image via account page

Goto account page and click on 'Choose File' and click save.

## Retrieve avatar image
```
 curl -v \
   -H "Authorization: Bearer $KC_ACCESS_TOKEN" \
   http://localhost:8080/auth/realms/$KC_REALM/avatar-provider/avatar \
   -o output.png
 ```