import software.amazon.awssdk.services.ecr.EcrClient
import software.amazon.awssdk.services.ecr.model.DescribeImagesRequest
import software.amazon.awssdk.services.ecr.model.ImageIdentifier
import software.amazon.awssdk.services.ecs.EcsClient
import software.amazon.awssdk.services.ecs.model.DescribeClustersRequest
import software.amazon.awssdk.services.ecs.model.DescribeTasksRequest
import software.amazon.awssdk.services.ecs.model.ListTasksRequest

data class RunningImage(val name: String, val image: String, val digest: String) {
    fun repoName(): String {
        return image.split("/", ":").get(1)
    }
}

fun main(args: Array<String>) {
    val ecs: EcsClient = EcsClient.create()
    val ecr: EcrClient = EcrClient.create()

    ecs.listClusters().clusterArns().forEach { cluster ->
        val clusterDescription = ecs.describeClusters(
            DescribeClustersRequest
                .builder()
                .clusters(cluster)
                .build()
        ).clusters().first()

        val tasks = ecs.listTasks(
            ListTasksRequest
                .builder()
                .cluster(cluster)
                .build()
        ).taskArns()

        val runningImages = ecs.describeTasks(
            DescribeTasksRequest
                .builder()
                .cluster(cluster)
                .tasks(tasks)
                .build()
        ).tasks()
            .flatMap { task ->
                task.containers()
                    .filter { container -> container.imageDigest() != null }
                    .map { container ->
                        RunningImage(
                            name = container.name(),
                            image = container.image(),
                            digest = container.imageDigest(),
                        )
                    }
            }

        val runningTags = runningImages
            .sortedBy { runningImage -> runningImage.name }
            .map {
                val tags = ecr.describeImages(
                    DescribeImagesRequest
                        .builder()
                        .repositoryName(it.repoName())
                        .imageIds(
                            ImageIdentifier
                                .builder()
                                .imageDigest(it.digest)
                                .build()
                        )
                        .build()
                ).imageDetails().flatMap { detail -> detail.imageTags() }
                Pair(it.name, tags)
            }.groupBy({ it.first }, { it.second.sorted().reversed() })

        println(clusterDescription.clusterName())
        runningTags.forEach {
            println("${it.key.padEnd(20)}\t${it.value}")
        }
        println("".padEnd(40, '_'))
    }

}