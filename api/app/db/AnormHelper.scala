package db

import anorm._
import java.util.UUID
import org.joda.time._
import org.joda.time.format._

private[db] object AnormHelper {
  implicit def rowToBigDecimal: Column[BigDecimal] = Column.nonNull { (value, meta) =>
    value match {
      case bd: java.math.BigDecimal => Right(BigDecimal(bd))
      case str: java.lang.String => Right(BigDecimal(str))
      case _ => Left(TypeDoesNotMatch(s"Cannot convert $value:${value.asInstanceOf[AnyRef].getClass} to BigDecimal for column ${meta.column}"))
    }
  }

  implicit val bigDecimalToStatement = new ToStatement[BigDecimal] {
    def set(s: java.sql.PreparedStatement, index: Int, aValue: BigDecimal): Unit = {
      s.setBigDecimal(index, new java.math.BigDecimal(aValue.toString))
    }
  }

  //ISODateTimeFormat.dateTimeParser does not support 'print' or 'printTo'
  //if you need to output date time use, for example, ISODateTimeFormat.dateTime for that
  //see app/models/package.scala
  val dateTimeFormatter = ISODateTimeFormat.dateTimeParser

  implicit def rowToDateTime: Column[DateTime] = Column.nonNull { (value, meta) =>
    value match {
      case ts: java.sql.Timestamp => Right(new DateTime(ts.getTime))
      case d: java.sql.Date => Right(new DateTime(d.getTime))
      case str: java.lang.String => Right(dateTimeFormatter.parseDateTime(str.trim))
      case _ => Left(TypeDoesNotMatch(s"Cannot convert $value:${value.asInstanceOf[AnyRef].getClass} to DateTime for column ${meta.column}"))
    }
  }

  implicit val dateTimeToStatement = new ToStatement[DateTime] {
    def set(s: java.sql.PreparedStatement, index: Int, aValue: DateTime): Unit = {
      s.setTimestamp(index, new java.sql.Timestamp(aValue.getMillis()) )
    }
  }

  implicit def rowToUUID: Column[UUID] = Column.nonNull[UUID] { (value, meta) =>
    value match {
      case v: UUID => Right(v)
      case _ => Left(TypeDoesNotMatch(s"Cannot convert $value:${value.asInstanceOf[AnyRef].getClass} to UUID for column ${meta.column}"))
    }
  }
}
