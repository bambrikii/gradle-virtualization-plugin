package org.bambrikii.gradle.virtualization.plugin.tasks;

import lombok.Setter;
import org.bambrikii.gradle.virtualization.plugin.extensions.DockerExtension;
import org.bambrikii.gradle.virtualization.plugin.utils.DockerUtils;
import org.gradle.api.Project;
import org.gradle.api.tasks.AbstractExecTask;
import org.gradle.api.tasks.TaskAction;

import java.util.ArrayList;
import java.util.List;

import static org.bambrikii.gradle.virtualization.plugin.utils.DockerUtils.buildRemoteRepoTag;
import static org.bambrikii.gradle.virtualization.plugin.utils.DockerUtils.ensureTagName;
import static org.bambrikii.gradle.virtualization.plugin.utils.LogUtils.logCommand;

@Setter
public class DockerPushTask extends AbstractExecTask<DockerPushTask> {
  public DockerPushTask() {
    super(DockerPushTask.class);
  }

  @TaskAction
  public void exec() {
    Project project = getProject();
    String version = project.getVersion().toString();
    DockerExtension ext = project.getExtensions().getByType(DockerExtension.class);

    String dockerCommand = DockerUtils.getDockerCommand(ext.getDockerCommand());
    String repo = ext.getRepo();
    String namespace = ext.getRepoNamespace();
    String tagName = ext.getTagName();

    String component = ensureTagName(project, tagName);

    push(dockerCommand, buildRemoteRepoTag(repo, namespace, component, version));
    push(dockerCommand, buildRemoteRepoTag(repo, namespace, component, "latest"));
  }

  private void push(String dockerCommand, String tagName) {
    List<String> args = new ArrayList<>();
    args.add(dockerCommand);
    args.add("image");
    args.add("push");
    args.add(tagName);

    commandLine(args);

    logCommand(getLogger(), getCommandLine());

    super.exec();
  }
}