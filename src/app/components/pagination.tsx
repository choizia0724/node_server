import React from "react";

interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

const Pagination = ({
  currentPage,
  totalPages,
  onPageChange,
}: PaginationProps) => {
  const pageGroupSize = 10;
  const currentGroup = Math.floor((currentPage - 1) / pageGroupSize); // 0부터 시작

  const startPage = currentGroup * pageGroupSize + 1;
  const endPage = Math.min(startPage + pageGroupSize - 1, totalPages);

  const handlePrevGroup = () => {
    const prevPage = startPage - 1;
    if (prevPage > 0) onPageChange(prevPage);
  };

  const handleNextGroup = () => {
    const nextPage = endPage + 1;
    if (nextPage <= totalPages) onPageChange(nextPage);
  };

  return (
    <div className="flex justify-center mt-4">
      {/* 이전 그룹 */}
      {startPage > 1 && (
        <button
          onClick={handlePrevGroup}
          className="px-3 py-1 border border-gray-300 rounded-l-md bg-white hover:bg-gray-100"
        >
          &lt;
        </button>
      )}

      {Array.from({ length: endPage - startPage + 1 }, (_, i) => {
        const page = startPage + i;
        return (
          <button
            key={page}
            onClick={() => onPageChange(page)}
            className={`px-3 py-1 border border-gray-300 
            ${
              page === currentPage
                ? "font-bold bg-blue-100"
                : "bg-white hover:bg-gray-100"
            } 
            ${i === 0 && startPage === 1 ? "rounded-l-md" : ""}
            ${page === endPage && endPage === totalPages ? "rounded-r-md" : ""}
            ${i !== 0 ? "-ml-px" : ""}  // 버튼 경계선 겹치게
          `}
          >
            {page}
          </button>
        );
      })}

      {endPage < totalPages && (
        <button
          onClick={handleNextGroup}
          className="px-3 py-1 border border-gray-300 rounded-r-md bg-white hover:bg-gray-100 -ml-px"
        >
          &gt;
        </button>
      )}
    </div>
  );
};

export default Pagination;
