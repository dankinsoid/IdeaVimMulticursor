package com.github.dankinsoid.multicursor

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

/**
 * Project-level service as recommended in https://plugins.jetbrains.com/docs/intellij/disposers.html#choosing-a-disposable-parent
 */
@Service(Service.Level.PROJECT)
class ProjectService(project: Project): Disposable {
   companion object {
       fun getInstance(project: Project): ProjectService {
           return project.service()
       }
   }

    override fun dispose() {
    }
}