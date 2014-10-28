package lib

import net.rcarz.jiraclient.{BasicCredentials, JiraClient}

object Jira {

  import scala.collection.JavaConverters._

  val client = new JiraClient(
    Config.requiredString("jira.url"),
    new BasicCredentials(Config.requiredString("jira.user"), Config.requiredString("jira.password"))
  )

  val issue = client.getIssue("PERFECTDAY-248")

  println("ISSUE: " + issue.getKey)
  println("Summary: " + issue.getSummary)
  println("Reporter: " + issue.getReporter)
  println("Reporter's Name: " + issue.getReporter.getDisplayName())

  // issue.addComment("No problem. We'll get right on it!");

  issue.getComments.asScala.foreach { c =>
    println(" COMMENT:")
    println("   - author: " + c.getAuthor)
    println("   - created: " + c.getCreatedDate)
    println("   - body: " + c.getBody)
  }

  issue.getAttachments.asScala.foreach { a =>
    println(" COMMENT:")
    println("   - author: " + a.getAuthor)
    println("   - created: " + a.getCreatedDate)
    println("   - filename: " + a.getFileName)
    println("   - url: " + a.getContentUrl)
    println("   - bytes: " + a.download().size)
  }


}
