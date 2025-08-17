export function Table({ children }: { children: React.ReactNode }) {
  return (
    <div className="overflow-x-auto rounded-lg shadow border border-gray-200">
      <table className="min-w-full divide-y divide-gray-200 text-sm text-left text-gray-700">
        {children}
      </table>
    </div>
  );
}
export function Thead({ children }: { children: React.ReactNode }) {
  return (
    <thead className="bg-gray-100 text-xs uppercase tracking-wider text-gray-600">
      <tr>{children}</tr>
    </thead>
  );
}
export function Tbody({ children }: { children: React.ReactNode }) {
  return (
    <tbody className="divide-y divide-gray-200 bg-white">{children}</tbody>
  );
}

export function Tr({ children }: { children: React.ReactNode }) {
  return <tr className="hover:bg-gray-50 transition">{children}</tr>;
}
export function Td({ children }: { children: React.ReactNode }) {
  return <td className="px-4 py-2">{children}</td>;
}

export function Th({ children }: { children: React.ReactNode }) {
  return <th className="px-4 py-3">{children}</th>;
}
