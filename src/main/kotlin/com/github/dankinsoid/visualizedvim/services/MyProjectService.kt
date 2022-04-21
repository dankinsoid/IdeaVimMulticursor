package com.github.dankinsoid.visualizedvim.services

import com.intellij.openapi.project.Project
import com.github.dankinsoid.visualizedvim.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
