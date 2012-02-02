/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.reporting.internal;

import groovy.lang.Closure;
import org.gradle.api.Task;
import org.gradle.api.internal.DirectInstantiator;
import org.gradle.api.internal.Instantiator;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.reporting.DefaultReportContainer;
import org.gradle.api.reporting.Report;
import org.gradle.api.reporting.ReportContainer;
import org.gradle.api.resources.ReadableResource;
import org.gradle.util.Configurable;
import org.gradle.util.ConfigureUtil;

import java.util.HashSet;
import java.util.Set;

public abstract class ReportContainerBuilder<C extends ReportContainer, R extends Report> implements Configurable<ReportContainerBuilder<C, R>> {

    private final Set<Report> reports = new HashSet<Report>();
    private final Instantiator instantiator;
    private final Class<? extends C> containerType;
    private final Class<? extends R> reportType;

    protected ReportContainerBuilder(Class<? extends C> containerType, Class<? extends R> reportType, Instantiator instantiator) {
        this.containerType = containerType;
        this.reportType = reportType;
        this.instantiator = instantiator;
    }
    
    public static <C extends ReportContainer, R extends Report> TaskReportContainerBuilder<C, R> forTask(Class<? extends C> containerType, Class<? extends R> reportType, Task task) {
        return new TaskReportContainerBuilder<C, R>(containerType, reportType, task);
    }
    
    public static class TaskReportContainerBuilder<C extends ReportContainer, R extends Report> extends ReportContainerBuilder<C, R> {
        private Task task;
               
        private TaskReportContainerBuilder(Class<? extends C> containerType, Class<? extends R> reportType, Task task) {
            super(containerType, reportType, ((ProjectInternal)task.getProject()).getServices().get(Instantiator.class));
            this.task = task;
        }
 
        public R singleFile(String name) {
            return super.singleFile(getReportType(), name);
        }

        public R multiFile(String name) {
            return super.multiFile(getReportType(), name);
        }
        
        @Override
        protected Object[] arrangeConstructionArgs(Class<?> clazz, String name, boolean multiFile, Object[] constructionArgs) {
            constructionArgs = super.arrangeConstructionArgs(clazz, name, multiFile, constructionArgs);
            Object[] constructionArgsPlusTask = new Object[constructionArgs.length + 1];
            
            int i = 0;
            for (Object arg : constructionArgs) {
                constructionArgsPlusTask[i++] = arg;
            }
            constructionArgsPlusTask[i] = task;
            return constructionArgsPlusTask;
        }
    }

    protected Class<? extends R> getReportType() {
        return reportType;
    }
    
    public <T extends Report> T singleFile(Class<T> clazz, String name, Object... constructionArgs) {
        return add(clazz, arrangeConstructionArgs(clazz, name, false, constructionArgs));
    }

    public <T extends Report> T multiFile(Class<T> clazz, String name, Object... constructionArgs) {
        return add(clazz, arrangeConstructionArgs(clazz, name, true, constructionArgs));
    }
    
    protected Object[] arrangeConstructionArgs(Class<?> clazz, String name, boolean multiFile, Object... constructionArgs) {
        Object[] args = new Object[constructionArgs.length + 2];
        args[0] = name;
        args[1] = multiFile;
        int i = 2;
        for (Object arg : constructionArgs) {
            args[i++] = arg;    
        }
        return args;
    }
    
    protected <T extends Report> T add(Class<T> clazz, Object[] constructionArgs) {
        T report = instantiator.newInstance(clazz, constructionArgs);
        reports.add(report);
        return report;
    }

    public ReportContainerBuilder<C, R> configure(Closure cl) {
        return ConfigureUtil.configure(cl, this, false);
    }

    public C build(Closure config) {
        configure(config);
        return build();
    }

    public C build() {
        return instantiator.newInstance(containerType, reportType, reports);
    }
}