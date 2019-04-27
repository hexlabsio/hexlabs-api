import io.hexlabs.kloudformation.module.serverless.Method
import io.hexlabs.kloudformation.module.serverless.Serverless
import io.hexlabs.kloudformation.module.serverless.serverless
import io.kloudformation.KloudFormation
import io.kloudformation.StackBuilder
import io.kloudformation.json
import io.kloudformation.resource.aws.ec2.securityGroup

class Stack : StackBuilder {
    override fun KloudFormation.create(args: List<String>) {
        serverless("hexlabs-api", "dev", +"hexlabs-deployments", privateConfig = Serverless.PrivateConfig(+listOf(securityGroup(+"HexLabs Lambda").GroupId()))) {
            serverlessFunction("hexlabs-api", +args.first(), +"org.http4k.serverless.lambda.LambdaFunction::handle", +"java8") {
                lambdaFunction {
                    timeout(30)
                    memorySize(2048)
                    environment {
                        variables(json(mapOf(
                            "HTTP4K_BOOTSTRAP_CLASS" to "io.hexlabs.api.api.RootApi"
                        )))
                    }
                }
                http(cors = true) {
                    path("") { Method.GET(); }
                    path("contact") { Method.POST(); }
                }
            }
        }
    }
}