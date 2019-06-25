/**
 * returns the number of element of an array, filtering out the headers
 * @returns a number
 */

export function filterCountNoHeaders() {
    return (input) => {
        return input?input.filter(function(e){return !e.header;}).length:0;
    }
}
