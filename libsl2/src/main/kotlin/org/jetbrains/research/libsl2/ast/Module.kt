package org.jetbrains.research.libsl2.ast

import org.jetbrains.research.libsl2.ast.decl.GlobalDecl
import org.jetbrains.research.libsl2.ast.decl.ImportDecl
import org.jetbrains.research.libsl2.location.Location
import org.jetbrains.research.libsl2.location.LocationProvider
import org.jetbrains.research.libsl2.resolve.scope.ModuleScope
import java.util.IdentityHashMap

data class Module(
    override var location: Location?,
    var header: Header?,
    var decls: MutableList<GlobalDecl>,
) : LocationProvider {
    // initialized during name resolution
    val scope = ModuleScope(this)
    val imports: MutableList<ImportDecl> = mutableListOf()
    val importedBy: MutableList<Pair<Module, ImportDecl>> = mutableListOf()

    /**
     * Walks the module import graph and returns its post-order.
     *
     * The returned list contains this module and all (transitively) imported modules (each occurring exactly once)
     * so that later modules import earlier modules unless there is an import cycle.
     */
    fun getModuleGraphPostOrder(): List<Module> {
        data class Task(val module: Module, var declIdx: Int = 0)

        val rpo = mutableListOf<Module>()
        val discoveredModules = IdentityHashMap<Module, Unit>()
        val taskStack = mutableListOf(Task(this))

        dfs@ while (taskStack.isNotEmpty()) {
            val task = taskStack.last()
            val module = task.module

            while (task.declIdx < module.decls.size) {
                val decl = module.decls[task.declIdx++]

                if (decl is ImportDecl && discoveredModules.put(decl.importedModule, Unit) == null) {
                    taskStack += Task(decl.importedModule)
                    continue@dfs
                }
            }

            rpo += module
            taskStack.removeLast()
        }

        return rpo
    }
}

fun Module.walk(visitor: Visitor) {
    for (decl in decls) {
        visitor.visit(decl)
    }
}
