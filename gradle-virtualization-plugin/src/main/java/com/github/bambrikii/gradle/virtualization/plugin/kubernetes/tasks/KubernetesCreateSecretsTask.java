package com.github.bambrikii.gradle.virtualization.plugin.kubernetes.tasks;

import com.github.bambrikii.gradle.virtualization.plugin.kubernetes.ext.KubernetesExtension;
import com.github.bambrikii.gradle.virtualization.plugin.kubernetes.ext.KubernetesSecretGroup;
import org.gradle.api.Project;
import org.gradle.api.tasks.AbstractExecTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.internal.ExecException;

import java.util.ArrayList;
import java.util.List;

import static com.github.bambrikii.gradle.virtualization.plugin.kubernetes.utils.KubernetesUtils.command;
import static com.github.bambrikii.gradle.virtualization.plugin.kubernetes.utils.KubernetesUtils.namespace;
import static com.github.bambrikii.gradle.virtualization.plugin.kubernetes.utils.KubernetesUtils.tryAddSecretFile;
import static com.github.bambrikii.gradle.virtualization.plugin.kubernetes.utils.KubernetesUtils.tryAddSecretLiteral;
import static com.github.bambrikii.gradle.virtualization.plugin.kubernetes.utils.KubernetesUtils.tryAddSecretType;
import static com.github.bambrikii.gradle.virtualization.plugin.utils.LogUtils.logCommand;

public class KubernetesCreateSecretsTask extends AbstractExecTask<KubernetesCreateSecretsTask> {
    public KubernetesCreateSecretsTask() {
        super(KubernetesCreateSecretsTask.class);
    }

    @TaskAction
    public void exec() {
        Project project = getProject();
        KubernetesExtension ext = project.getExtensions().getByType(KubernetesExtension.class);

        List<KubernetesSecretGroup> secretGroups = ext.getSecretGroups();
        if (secretGroups == null || secretGroups.isEmpty()) {
            return;
        }

        secretGroups.forEach(secret -> create(ext, secret));
    }

    private void create(KubernetesExtension ext, KubernetesSecretGroup secretGroup) {
        List<String> args = new ArrayList<>();
        command(ext, args);
        args.add("create");
        args.add("secret");
        args.add("generic");
        args.add(secretGroup.getName());

        secretGroup.getSecrets().forEach(secret -> {
            tryAddSecretLiteral(secret, args);
            tryAddSecretFile(secret, args);
            tryAddSecretType(secret, args);
        });

        namespace(ext, args);

        commandLine(args);
        logCommand(getLogger(), getCommandLine());

        try {
            super.exec();
        } catch (ExecException ex) {
            getLogger().lifecycle(ex.getMessage());
        }
    }
}
