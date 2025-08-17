"use client";

import axios from "axios";
import type { StockDTO } from "@/types/stock";
import { Table, Thead, Th, Tbody, Tr, Td } from "./components/Table";
import { useEffect, useState } from "react";
// src/app/page.tsx

interface TableWidgetProps {
  data: StockDTO[];
}

const TableWidget = (props: TableWidgetProps) => {
  console.log("TableWidget data:", props);
  return (
    <Table>
      <Thead>
        <Th>구분코드</Th>
        <Th>이름</Th>
        <Th>기준일</Th>
        <Th>상장구분</Th>
        <Th>시장구분</Th>
        <Th>법인등록번호</Th>
        <Th>법인명</Th>
      </Thead>
      <Tbody>
        {props.data.length > 0 &&
          props.data.map((item, idx) => (
            <Tr key={idx}>
              <Td>{item.symbol}</Td>
              <Td>{item.name}</Td>
              <Td>{item.basdt}</Td>
              <Td>{item.isincd}</Td>
              <Td>{item.mrktctg}</Td>
              <Td>{item.crno}</Td>
              <Td>{item.corpnm}</Td>
            </Tr>
          ))}
      </Tbody>
    </Table>
  );
};

const getStockData = async (filters: {
  symbol?: string;
  name?: string;
  basdt?: string;
  isincd?: string;
  mrktctg?: string;
  crno?: string;
  corpnm?: string;
}): Promise<StockDTO[]> => {
  const res = await axios.post<StockDTO[]>("/api/stock/search", filters, {
    headers: {
      "Cache-Control": "no-cache",
    },
  });

  return res.data;
};

export default function Home() {
  const [stockData, setStockData] = useState<StockDTO[]>([]);
  const [filters, setFilters] = useState({
    symbol: "",
    name: "",
    basdt: "",
    isincd: "",
    mrktctg: "",
    crno: "",
    corpnm: "",
    page: 1,
    size: 10,
  });

  useEffect(() => {
    getStockData(filters)
      .then((data) => {
        setStockData(data);
      })
      .catch((error) => {
        console.error("Error fetching stock data:", error);
      });
  }, [filters]);
  return stockData.length > 0 ? <TableWidget data={stockData} /> : null;
}
