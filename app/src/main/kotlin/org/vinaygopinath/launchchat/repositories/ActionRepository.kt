package org.vinaygopinath.launchchat.repositories

import org.vinaygopinath.launchchat.daos.ActionDao
import javax.inject.Inject

class ActionRepository @Inject constructor(
    private val actionDao: ActionDao
) {

}