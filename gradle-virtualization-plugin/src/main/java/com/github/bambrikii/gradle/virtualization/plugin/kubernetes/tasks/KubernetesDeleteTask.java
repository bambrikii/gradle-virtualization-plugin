package com.github.bambrikii.gradle.virtualization.plugin.kubernetes.tasks;

import com.github.bambrikii.gradle.virtualization.plugin.kubernetes.ext.KubernetesExtension;
import groovy.lang.GString;
import org.gradle.api.Project;
import org.gradle.api.tasks.AbstractExecTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.github.bambrikii.gradle.virtualization.plugin.kubernetes.utils.KubernetesUtils.DEPLOYMENT_FILE;
import static com.github.bambrikii.gradle.virtualization.plugin.kubernetes.utils.KubernetesUtils.SERVICE_FILE;
import static com.github.bambrikii.gradle.virtualization.plugin.kubernetes.utils.KubernetesUtils.command;
import static com.github.bambrikii.gradle.virtualization.plugin.kubernetes.utils.KubernetesUtils.getKubernetesDir;
import static com.github.bambrikii.gradle.virtualization.plugin.kubernetes.utils.KubernetesUtils.namespace;
import static com.github.bambrikii.gradle.virtualization.plugin.utils.IOUtils.PATH_SEPARATOR;
import static com.github.bambrikii.gradle.virtualization.plugin.utils.LogUtils.logCommand;

public class KubernetesDeleteTask extends AbstractExecTask<KubernetesDeleteTask> {
    public KubernetesDeleteTask() {
        super(KubernetesDeleteTask.class);
    }

    @TaskAction
    public void exec() {
        Project project = getProject();

        KubernetesExtension ext = project.getExtensions().getByType(KubernetesExtension.class);

        String path = getKubernetesDir(getWorkingDir()) + PATH_SEPARATOR;

        List<GString> resources = new ArrayList<>((List) ext.getResources());
        Collections.reverse(resources);
        boolean resourcesFound = resources != null && !resources.isEmpty();
        if (resourcesFound) {
            resources.forEach(resource -> exec(ext, resource.toString()));
        }

        String resource = ext.getResource();
        boolean resourceFound = resource != null && !resource.isBlank();
        if (resourceFound) {
            exec(ext, resource);
        }

        if (!resourcesFound && !resourceFound) {
            tryExec(ext, path + SERVICE_FILE);
            tryExec(ext, path + DEPLOYMENT_FILE);
        }
    }

    private void tryExec(KubernetesExtension ext, String file) {
        if (!new File(file).exists()) {
            return;
        }

        exec(ext, file);
    }

    private void exec(KubernetesExtension ext, String file) {
        List<String> args = new ArrayList<>();

        command(ext, args);
        namespace(ext, args);
        delete(args, file);

        commandLine(args);
        logCommand(getLogger(), getCommandLine());

        super.exec();
    }

    private void delete(List<String> args, String kubernetesDeploymentFile) {
        args.add("delete");
        args.add("-f");
        args.add(kubernetesDeploymentFile);
    }
}
