export const web = {
  apiUrl: '${apiUrl ?: ""}',
  baseUrl: '${baseUrl ?: ""}'
}

export const aws = {
  Auth: {
    region: '${awsRegion ?: "us-east-1"}',
    userPoolWebClientId: '${awsUserPoolClientId ?: ""}',
    userPoolId: '${awsUserPoolId ?: ""}'
  }
}
