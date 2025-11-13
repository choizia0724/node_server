/**
 * @fileoverview
 * @filename stockSearchRequest.tsx
 * @author zia
 * @version 1.0.0 - 2025. 11. 12.
 * @copyright 2025,
 */
export interface stockSearchRequest{
    symbol: string,
    name: string,
    mrktctg: string,
    from: string,
    to: string,
    page: number,
    useornot: boolean
}