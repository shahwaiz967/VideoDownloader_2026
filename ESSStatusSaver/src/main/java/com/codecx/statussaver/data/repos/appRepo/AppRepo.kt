package com.codecx.statussaver.data.repos.appRepo

import com.codecx.statussaver.data.models.StatusItem

interface AppRepo {
  suspend  fun loadStatus(): List<StatusItem>

}