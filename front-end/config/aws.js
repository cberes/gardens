export default {
  Auth: {
    region: '${awsRegion ?: "us-east-1"}',
    userPoolWebClientId: '${awsUserPoolClientId ?: ""}',
    userPoolId: '${awsUserPoolId ?: ""}'
  }
}
