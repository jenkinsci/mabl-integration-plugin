![mabl logo](https://avatars3.githubusercontent.com/u/25963599?s=100&v=4)
# mabl Jenkins Plugin
[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/mabl-integration-plugin/master)](https://ci.jenkins.io/job/Plugins/job/mabl-integration-plugin/job/master/)

This plugin allows easy launching of [mabl](https://www.mabl.com) journeys as a step in your Jenkins build. Your Jenkins build outcome will be tied to that of your deployment event.

See [**official mabl plugin site**](https://plugins.jenkins.io/mabl-integration) for documentation.

# Plugin Installation
Install the [plugin](https://plugins.jenkins.io/mabl-integration) into your Jenkins `v1.580+` server from the *Available Plugins* tab by searing for "mabl".

## Building from Source

1. Clone this repo
2. build with `mvn clean package`
3. Copy the plugin in `target/mabl-integration.hpi` to your Jenkins `plugins/` directory
4. Restart Jenkins

You can also install the `.hpi` file from the web UI by visting
**Jenkins > Manage Jenkins > Manager Plugins > Advanced > Upload Plugin**.

## Creating a mabl Build Step

1. Create or edit a Jenkins project
2. Select **Run mabl journeys** from the **Add build step** drop down list
3. Copy your API key, `environment_id`, and `application_id` from the [API Settings Page](https://help.mabl.com/v1.0/docs/triggering-tests-via-the-api)
4. Save and run your build

## Local Development
### Jenkins Docker
Overview of how to launch a Jenkins Docker instance with Jenkins, then build the plugin and deploy it that instance.

*Note:* Please check [Jenkins Hub](https://hub.docker.com/r/jenkins/jenkins)
for any specific image version before pulling Jenkins image.

```bash
# Launch Jenkins container and automatically pull the image if not present
docker run -d -p 9090:8080 --name=jenkins-master jenkins/jenkins

# Run command in the running container
docker exec -it jenkins-master bash

# Find initialAdminPassword to unlock Jenkins on a browser at localhost:9090
cat /var/jenkins_home/secrets/initialAdminPassword

# Setup your Jenkins instance

# Build and deploy plugin to Jenkins (make sure you're in the mabl-integration-plugin directory)
mvn clean package \
  && docker cp target/mabl-integration.hpi jenkins-master:/var/jenkins_home/ \
  && docker restart jenkins-master
```

### Local Machine
Overview of how to run/debug plugin with Intellij on local machine.

- Open the project in Intellij
- Look for Edit Configurations under Run
- Add Maven template
- Configure your working directory to point to your project path similarly as
the screenshot below:
![Screenshot](img/Intellij-config.png)
- Click OK and then run/debug the plugin
- Wait until you see

        INFO: Jenkins is fully up and running

- Log into this URL on your web browser

        localhost:8090/jenkins
Now you can test your plugin.

## Deployment

Before making a new plugin release, ensure code is in high quality, fully tested state. See [extra checks](https://wiki.jenkins.io/display/JENKINS/Plugin+Release+Tips).

1. Update your `~/.m2/settings.xml` according to the [Jenkins docs](https://wiki.jenkins.io/display/JENKINS/Hosting+Plugins#HostingPlugins-Releasingtojenkins-ci.org).
2. Setup and run a GitHub [ssh agent](https://help.github.com/articles/generating-a-new-ssh-key-and-adding-it-to-the-ssh-agent/#adding-your-ssh-key-to-the-ssh-agent).
3. Run `mvn release:prepare release:perform -B` from the HEAD of master
4. Run `mvn deploy` on sucess of above step.

Wait ~8 hours for plugin to become GA across all Jenkins instances under the "Available Plugins" listing.
