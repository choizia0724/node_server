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

const getStockData = async (): Promise<StockDTO[]> => {
  const res = await axios.get<StockDTO[]>("/api/stock");

  return res.data;
};

export default function Home() {
  const [stockData, setStockData] = useState<StockDTO[]>([]);

  useEffect(() => {
    getStockData()
      .then((data) => {
        setStockData(data);
      })
      .catch((error) => {
        console.error("Error fetching stock data:", error);
      });
  }, []);
  return stockData.length > 0 ? <TableWidget data={stockData} /> : null;
}
