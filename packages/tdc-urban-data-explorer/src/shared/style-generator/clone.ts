// Deep clone function used by StyleGenerator in node and frontend
// From https://github.com/codeandcats/fast-clone

export function clone(value) {
  var type = typeof value;
  switch (type) {
    case 'object':
      // null and undefined
      if (value == null) {
        return value;
      }

      var result;

      if (value instanceof Date) {
        result = new Date();
        result.setTime(value.getTime());
        return result;
      }
      else if (value instanceof RegExp) {
        result = newRegExp(value);
        return result;
      }

      result = JSON.parse(JSON.stringify(value));
      fixTypes(value, result);
      return result;

    default:
      return value;
  }
}

function fixPropertyValue(original, copy, key) {
  var originalValue = original[key];
  var originalType = typeof originalValue;

  switch (originalType) {
    case 'object':
      if (originalValue instanceof Date) {
        var newValue = new Date();
        newValue.setTime(originalValue.getTime());
        copy[key] = newValue;
      }
      else if (originalValue instanceof RegExp) {
        copy[key] = newRegExp(originalValue);
      }
      else if (originalValue == null) {
        copy[key] = originalValue;
      }
      else {
        fixTypes(originalValue, copy[key]);
      }
      break;

    case 'number':
      if (isNaN(originalValue)) {
        copy[key] = NaN;
      }
      else if (originalValue === Infinity) {
        copy[key] = Infinity;
      }
      break;

    default:
      break;
  }
}

function fixTypes(original, copy) {
  if (original instanceof Array) {
    for (var index = 0; index < original.length; index++) {
      fixPropertyValue(original, copy, index);
    }
  }
  else {
    var keys = Object.getOwnPropertyNames(original);
    keys.forEach(function(key) {
      fixPropertyValue(original, copy, key);
    });
  }
}

function newRegExp(value) {
  var regexpText = String(value);
  var slashIndex = regexpText.lastIndexOf('/');
  return new RegExp(regexpText.slice(1, slashIndex), regexpText.slice(slashIndex + 1));
}
