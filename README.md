# hello-cloud-functions-cljs

[Nbb](https://github.com/babashka/nbb) on Google Cloud Functions 2nd generation.

## Installation

```sh
npm install
```

## Test

### Integration tests

Run integration tests with [SuperTest](https://github.com/visionmedia/supertest) and [Jest](https://github.com/facebook/jest):

```sh
npm run test
```

## Development

### Invoke the function locally

In one terminal, start the function:

```sh
npm run start:development
```

In another terminal, call the function with curl, Postman, etc:

```sh
curl -X POST \
-L "$FUNCTION_URL" \
-H "Content-Type: application/json" \
--data-raw '{
    "text": "Hello world in <b>bold</b> and <i>italic</i>"
}'
```

Note: here `FUNCTION_URL` is `http://localhost:PORT` (e.g. `http://localhost:3000`).

## Deploy

In order to deploy the function you will need the [gcloud CLI](https://cloud.google.com/sdk/docs/install). You will also need [Babashka](https://github.com/babashka/babashka) if you want to deploy the function using the Babashka task `deploy`.

```sh
# using a npm script
npm run deploy

# or, using a Babashka task
bb run deploy
```

### Invoke the function deployed to GCP Cloud Functions

If you want to test the function with curl (or Postman, etc) don't forget to include the identity token in the `Authorization` header.

If you invoke the function without the `Authorization` header, [Google Front End](https://cloud.google.com/docs/security/infrastructure/design#google_front_end_service) will block your request and return you a HTTP 403:

```sh
curl -X POST \
-i \
-L "$FUNCTION_URL" \
-H "Content-Type: application/json" \
--data-raw '{
    "text": "This is a message sent by <b>curl</b>"
}'
```

You can make an authenticated request using any account which is authorized to invoke that function.

Here is how you can make an authenticated request using the **user account** you are currently logged in:

```sh
curl -X POST \
-L "$FUNCTION_URL" \
-H "Authorization: Bearer $(gcloud auth print-identity-token)" \
-H "Content-Type: application/json" \
--data-raw '{
    "text": "This is a message sent by <b>curl</b>"
}'
```

Note: here `FUNCTION_URL` is the [trigger URL](https://cloud.google.com/functions/docs/calling/http) of the function deployed to Cloud Functions (e.g. `https://hello-45eyyotfta-ey.a.run.app`).

Here is how you can make an authenticated request using a **service account**, thanks to the [service account impersonation](https://cloud.google.com/iam/docs/impersonating-service-accounts) (the service account you impersonate must have the permissions to invoke the function):

```sh
ID_TOKEN=$(gcloud auth print-identity-token --impersonate-service-account sa-notifier@prj-kitchen-sink.iam.gserviceaccount.com) &&
curl -X POST \
-L "$FUNCTION_URL" \
-H "Authorization: Bearer $ID_TOKEN" \
-H "Content-Type: application/json" \
--data-raw '{
    "text": "This is a message sent by <b>curl</b>"
}'
```

## See also

- [Nbb on Google Cloud Functions (1st generation)](https://github.com/babashka/nbb/blob/main/doc/gcloud_functions.md)
