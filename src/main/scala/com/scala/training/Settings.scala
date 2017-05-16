package com.scala.training

import com.typesafe.config.Config


class Settings(config:Config){
  def host=config.getString("host")
  def port=config.getInt("port")
  def dbHost=config.getString("dbHost")
  def dbPort=config.getInt("dbPort")
  def dbName=config.getString("db.name")
  def dbUrl=config.getString("db.url")
  def dbTable=config.getString("db.tableName")
  def conf=config
}
object Settings {
  def apply(config: Config): Settings = new Settings(config)
}
