/**
 * Utility functions
 */

/**
 * Copyright © 2018 Emu Analytics
 */

/**
 * Start a hi-resolution timer
 *
 * const timer = startTimer();
 * ...
 * ...
 * const duration = timer();
 *
 */
export function startTimer() {
  var start = process.hrtime();
  return function endTimer() {
    var diff = process.hrtime(start);

    return (diff[0] * 1e9 + diff[1]) / 1e6;
  };
}

/**
 * Return textual representation of a Postgres OID
 */
export function postgresTypeName(type: number): string {
  switch (type) {
    case 20:
      return 'BigInteger'; // int8
    case 21:
      return 'Integer'; // int2
    case 23:
      return 'Integer'; // int4
    case 25:
      return 'Text'; // int4
    case 26:
      return 'Integer'; // oid
    case 700:
      return 'Float'; // float4/real
    case 701:
      return 'Float'; // float8/double
    case 16:
      return 'Bool';
    case 1082:
      return 'Date'; // date
    case 1114:
      return 'Date'; // timestamp without timezone
    case 1184:
      return 'Date'; // timestamp
    case 600:
      return 'Point'; // point
    case 651:
      return 'StringArray'; // cidr[]
    case 718:
      return 'Circle'; // circle
    case 1000:
      return 'BoolArray';
    case 1001:
      return 'ByteAArray';
    case 1005:
      return 'IntegerArray'; // _int2
    case 1007:
      return 'IntegerArray'; // _int4
    case 1028:
      return 'IntegerArray'; // oid[]
    case 1016:
      return 'BigIntegerArray'; // _int8
    case 1017:
      return 'PointArray'; // point[]
    case 1021:
      return 'FloatArray'; // _float4
    case 1022:
      return 'FloatArray'; // _float8
    case 1231:
      return 'FloatArray'; // _numeric
    case 1014:
      return 'StringArray'; //char
    case 1015:
      return 'StringArray'; //varchar
    case 1008:
      return 'StringArray';
    case 1009:
      return 'StringArray';
    case 1040:
      return 'StringArray'; // macaddr[]
    case 1041:
      return 'StringArray'; // inet[]
    case 1115:
      return 'DateArray'; // timestamp without time zone[]
    case 1182:
      return 'DateArray'; // _date
    case 1185:
      return 'DateArray'; // timestamp with time zone[]
    case 1186:
      return 'Interval';
    case 17:
      return 'ByteA';
    case 114:
      return 'JSON'; // json
    case 3802:
      return 'JSONB'; // jsonb
    case 199:
      return 'JsonArray'; // json[]
    case 3807:
      return 'JsonbArray'; // jsonb[]
    case 3907:
      return 'StringArray'; // numrange[]
    case 2951:
      return 'StringArray'; // uuid[]
    case 791:
      return 'StringArray'; // money[]
    case 1183:
      return 'StringArray'; // time[]
    case 1270:
      return 'StringArray';
    case 1700:
      return 'Numeric';
    case 2950:
      return 'UUID';
    default:
      return `Custom type ${type}`;
  }
}

export function isAuthenticated(req, res, next) {

  if (req.user)
    return next();

  res.status(401).send('Not Authorized');
}
