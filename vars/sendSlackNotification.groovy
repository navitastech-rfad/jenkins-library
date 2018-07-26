#!/usr/bin/env groovy

/**
 * Send notifications based on build status string
 *
 */
def call(String buildStatus = 'STARTED', String message = null, Boolean pr = true) {
  // build status of null means successful
  buildStatus =  buildStatus ?: 'SUCCESS'

  // Default values
  def color = 'RED'
  def colorCode = '#E74C3C'
  def printChanges = false

  // Override default values based on build status
  if (buildStatus == 'STARTED') {
    color = 'YELLOW'
    colorCode = '#FFFF00'
    buildStatus = 'Started'
  } else if (buildStatus == 'SUCCESS') {
    color = 'GREEN'
    colorCode = '#27AE60'
    buildStatus = 'Successful'
    printChanges = true
  } else if (buildStatus == 'ABORTED') {
    color = 'GREY'
    colorCode = '#D7DBDD'
    buildStatus = 'Aborted'
  } else if (buildStatus == 'ARCHIVE_ERROR') {
    // Use default colors
    buildStatus = 'Archiving failed'
  } else if (buildStatus == 'FAILURE') {
    buildStatus = 'FAILED'
    printChanges = true
  } else if (buildStatus == 'UNSTABLE') {
    color = 'YELLOW'
    colorCode = '#FFFF00'
    buildStatus = 'Unstable'
    printChanges = true
  }

  String buildResultUrl = "${env.BUILD_URL}"


  // Slack
  string jobNameSafe = "${env.JOB_NAME}"
  jobNameSafe = jobNameSafe.replaceAll("%2F", "/")
  String slackMsg = "<${buildResultUrl}|${jobNameSafe} #${env.BUILD_NUMBER}>: *${buildStatus}*\n"

    if(pr)
  {
    String prMsg = "\n Please review the pull request ${env.CHANGE_URL} on branch ${env.BRANCH_NAME}"

    slackMsg =slackMsg + prMsg

  }



   

  echo("Sending slack message: ${slackMsg}")

  // I put this in for cases where Slack doesn't work - let the build continue
  try
  {
    slackSend (color: colorCode, message: slackMsg)
  }
  catch (error)
  {
    echo "Slack notification failed: $error"
    currentBuild.result = 'UNSTABLE'
  }
}
 