package com.jangni.socket.scala

trait IContext {

  implicit class JobContextValue(jobContext: JobContext) {

    def fromValues(key: String): String = jobContext.contextValues.getOrElse(key, "")

    def toValues(key: String, value: String): Option[String] = jobContext.contextValues.put(key, value)

    def thridLsId: String = fromValues("thridLsId")

    def thridLsId_=(thridLsId: String): Unit = toValues("thridLsId", thridLsId)

    def respCode: String = fromValues("respCode")

    def respCode_=(respCode: String): Unit = toValues("respCode", respCode)

    def respDesc: String = fromValues("respDesc")

    def respDesc_=(respDesc: String): Unit = toValues("respDesc", respDesc)


    private def fromObjects[T](key: String): Option[T] = jobContext.contextObjects.get(key).asInstanceOf[Option[T]]

    private def toObjects(key: String, value: Object): Option[Object] = jobContext.contextObjects.put(key, value)

    def getPackXml: Option[String] = jobContext.contextObjects.remove("getPackXml").asInstanceOf[Option[String]]

    def getPackXml_=(getPackXml: String): Unit = toObjects("getPackXml", getPackXml)

    def putPackXml: Option[String] = jobContext.contextObjects.remove("putPackXml").asInstanceOf[Option[String]]

    def putPackXml_=(putPackXml: String): Unit = toObjects("putPackXml", putPackXml)

  }

}
