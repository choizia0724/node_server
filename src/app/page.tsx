"use client";

import axios from "axios";
import type { StockDTO } from "@/types/stock";
import { Table, Thead, Th, Tbody, Tr, Td } from "./components/Table";
import { useEffect, useState } from "react";
import Pagination from "./components/pagination";
import Link from "next/link";

interface StockResponse {
  data: StockDTO[];
  pagination?: {
    currentPage: number;
    totalPages: number;
  };
}

interface TableWidgetProps {
  data: StockResponse;
}

const TableWidget = ({ data }: TableWidgetProps) => {
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
        {data.data.map((item, idx) => {
          const code = item.symbol.slice(-6);
          return(
          <Tr key={idx}>
            <Td>{item.symbol}</Td>
            <Td><Link href={`/stocks/${code}`}>{item.name}</Link></Td>
            <Td>{item.basdt}</Td>
            <Td>{item.isincd}</Td>
            <Td>{item.mrktctg}</Td>
            <Td>{item.crno}</Td>
            <Td>{item.corpnm}</Td>
          </Tr>
        )}
        )}
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
  page?: number;
  size?: number;
}): Promise<StockResponse> => {
  const res = await axios.post<StockResponse>("/api/stocks/search", filters, {
    headers: {
      "Cache-Control": "no-cache",
      "Content-Type": "application/json",
    },
  });

  return res.data;
};

export default function Home() {
  const [stockData, setStockData] = useState<StockResponse>({
    data: [],
    pagination: {
      currentPage: 1,
      totalPages: 1,
    },
  });

  const [filters, setFilters] = useState({
    symbol: "",
    name: "",
    basdt: "",
    isincd: "",
    mrktctg: "KOSPI",
    crno: "",
    corpnm: "",
    page: 1,
    limit: 10,
  });

  const handlePageChange = (page: number) => {
    setFilters((prev) => ({ ...prev, page }));
  };

  useEffect(() => {
    getStockData(filters)
      .then((data) => {
        setStockData(data);
      })
      .catch((error) => {
        console.error("Error fetching stock data:", error);
      });
  }, [filters]);

  return (
    <>
      <TableWidget data={stockData} />
      <Pagination
        currentPage={stockData.pagination?.currentPage ?? 1}
        totalPages={stockData.pagination?.totalPages ?? 1}
        onPageChange={handlePageChange}
      />
    </>
  );
}
