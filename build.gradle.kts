import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.hexlabs.kloudformation.module.serverless.Method
import io.hexlabs.kloudformation.module.serverless.Serverless
import io.hexlabs.kloudformation.module.serverless.serverless
import io.kloudformation.json
import io.kloudformation.model.KloudFormationTemplate
import io.kloudformation.resource.aws.ec2.securityGroup
import io.klouds.kloudformation.gradle.plugin.KloudFormationConfiguration
import io.klouds.kloudformation.gradle.plugin.Stack
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

buildscript {
    repositories {
        maven("https://dl.bintray.com/hexlabsio/kloudformation")
    }
    dependencies {
        classpath("io.hexlabs:kloudformation-serverless-module:0.1.13")
    }
}

plugins {
    kotlin("jvm") version "1.3.21"
    id("org.jlleitschuh.gradle.ktlint") version "7.1.0"
    id("com.github.johnrengelman.shadow") version "4.0.4"
    id("io.klouds.kloudformation.gradle.plugin") version "0.1.4"
}

fun version(): String {
    val buildNumber = System.getProperty("BUILD_NUM")
    val version = "0.1" + if (buildNumber.isNullOrEmpty()) "-SNAPSHOT" else ".$buildNumber"
    println("building version $version")
    return version
}

val projectVersion = version()

group = "io.hexlabs"
val artifactId = "hexlabs-api"
version = projectVersion

val http4kVersion = "3.115.1"

repositories {
    jcenter()
    mavenCentral()
    maven("https://dl.bintray.com/hexlabsio/kloudformation")
}

val shadowJar by tasks.getting(ShadowJar::class) {
    archiveClassifier.set("uber")
}

sourceSets["test"].java.srcDir("stack")

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(kotlin("reflect"))
    compile("org.http4k:http4k-core:$http4kVersion")
    compile("org.http4k:http4k-format-jackson:$http4kVersion")
    compile("org.http4k:http4k-serverless-lambda:$http4kVersion")

    testImplementation("io.kloudformation:kloudformation:0.1.119")
    testImplementation("io.hexlabs:kloudformation-serverless-module:0.1.13")

    testImplementation("io.mockk:mockk:1.9.2.kotlin12")
    testImplementation(group = "org.jetbrains.kotlin", name = "kotlin-test-junit5", version = "1.3.21")
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = "1.3.21")
    testRuntime(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = "5.0.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

artifacts {
    add("archives", shadowJar)
}

configure<KtlintExtension> {
    reporters.set(setOf(ReporterType.CHECKSTYLE, ReporterType.JSON))
}

tasks.findByPath("uploadDeploymentResources")!!.dependsOn += shadowJar

configure<KloudFormationConfiguration> {
    stacks = listOf(
        Stack(
            stackName = "hexlabs-api",
            template = { args -> KloudFormationTemplate.create {
                serverless("hexlabs-api", "dev", +"hexlabs-deployments", privateConfig = Serverless.PrivateConfig(+listOf(securityGroup(+"HexLabs Lambda").GroupId()))) {
                    serverlessFunction("hexlabs-api", +args.getValue("codeLocation"), +"org.http4k.serverless.lambda.LambdaFunction::handle", +"java8") {
                        lambdaFunction {
                            timeout(30)
                            memorySize(2048)
                            environment {
                                variables(
                                    json(mapOf(
                                        "HTTP4K_BOOTSTRAP_CLASS" to "io.hexlabs.api.api.RootApi"
                                    ))
                                )
                            }
                        }
                        http(cors = true) {
                            httpBasePathMapping(+"api.hexlabs.io", +"web")
                            path("/") {
                                Method.GET()
                                path("contact") { Method.POST(); }
                            }
                        }
                    }
                }
            }},
            region = "eu-west-1",
            uploadDeploymentResources = true,
            uploadLocation = "${shadowJar.archiveFile.get().asFile.absolutePath}",
            uploadBucket = "hexlabs-deployments"
        )
    )
}