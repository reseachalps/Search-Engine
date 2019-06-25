/**
 * Sort filter which works with the special characters (french accents, etc).
 * Works the same way as orderBy (probably a bit slower)
 * @returns {function(any): *}
 */
export function localeCompareString() {
    return (items,value) => {
        items.sort((a: any, b: any) => {
            return a[value] && a[value].localeCompare(b[value]) || -1;
        });
        return items;
    }
}