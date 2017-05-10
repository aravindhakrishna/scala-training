package com.scala.training

import com.typesafe.config.Config


class Settings(config:Config){
  def dbHost=config.getString("host")
  def dbPort=config.getInt("port")
  def dbName=config.getString("db.name")
  def dbUrl=config.getString("db.url")
  def conf=config
}
object Settings {
  def apply(config: Config): Settings = new Settings(config)
}
