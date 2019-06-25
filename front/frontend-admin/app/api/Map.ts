namespace api {
    export interface Map<KV,T> {
        [K: string]: T;
    }
}